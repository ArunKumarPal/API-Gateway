package com.arun.sample.gateway.filter;

import com.arun.sample.gateway.constants.ErrorCode;
import com.arun.sample.gateway.exception.ApiGatewayException;
import com.arun.sample.gateway.model.Pair;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.arun.sample.gateway.constants.Constants.*;
import static org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter.*;

@Component
@Primary
@ConfigurationProperties("spring.cloud.gateway.redis-rate-limiter")
public class CustomRateLimiter extends AbstractRateLimiter<CustomRateLimiter.Config> implements ApplicationContextAware {

    private ReactiveStringRedisTemplate redisTemplate;
    private RedisScript<List<Long>> script;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private final Cache<String, String> caffieneCache;

    private final Config defaultConfig;

    private final int defaultTrialUserTps = 50;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ip.based.rate-limit}")
    private int ipRateLimit;

    public CustomRateLimiter(Cache<String, String> caffieneCache, ReactiveStringRedisTemplate redisTemplate,
                             RedisScript<List<Long>> script, ConfigurationService configurationService
    ) {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, configurationService);
        this.redisTemplate = redisTemplate;
        this.script = script;
        this.caffieneCache = caffieneCache;
        this.defaultConfig = Config.of(100, 100, 1);
    }



    Mono<Pair<String, Config>> loadConfiguration(String routeId, String id) {
        Config routeConfig = Optional.ofNullable(
                Optional.ofNullable(getConfig().getOrDefault(routeId, defaultConfig))
                        .orElse(getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS))
        ).orElseThrow(() -> new IllegalArgumentException(String.format("No Configuration found for route %s or defaultFilters", routeId)));

        String userKey = Optional.ofNullable(routeConfig.getGroupId()).orElse(routeId) + "_" + id;
        return Mono.just(Pair.of(userKey, routeConfig));
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        if (!this.initialized.get()) {
            throw new IllegalStateException("RedisRateLimiter is not initialized");
        }
        if (id.startsWith(IP_BASED_RATE_LIMIT_PREFIX)) {
            return isAllowedForIPRateLimit(routeId, id);
        }

        return loadConfiguration(routeId, id).flatMap(keyAndRouteConfig -> {
            var keyId = keyAndRouteConfig.t1();
            var routeConfig = keyAndRouteConfig.t2();

            String replenishRate = String.valueOf(routeConfig.getReplenishRate());
            String burstCapacity = String.valueOf(routeConfig.getBurstCapacity());
            String requestedTokens = String.valueOf(routeConfig.getRequestedTokens());

            try {
                List<String> keys = getKeys(keyId);

                // The arguments to the LUA script. time() returns unixtime in seconds.
                List<String> scriptArgs = Arrays.asList(replenishRate, burstCapacity, "", requestedTokens);
                // allowed, tokens_left = redis.eval(SCRIPT, keys, args)
                Flux<List<Long>> flux = this.redisTemplate.execute(this.script, keys, scriptArgs);

                return flux.onErrorResume(throwable -> {
                    logger.debug("Error calling rate limiter lua", throwable);
                    return Flux.just(Arrays.asList(1L, -1L));
                }).reduce(new ArrayList<Long>(), (longs, l) -> {
                    longs.addAll(l);
                    return longs;
                }).map(results -> {
                    boolean allowed = results.get(0) == 1L;
                    Long tokensLeft = results.get(1);
                    logger.debug("Tokens left: {}", tokensLeft);
                    logger.debug("Allowed: {}", allowed);

                    Response response = new Response(allowed, getHeaders(routeConfig, tokensLeft));

                    logger.debug("response: {}", response);
                    return response;
                });
            } catch (Exception e) {
                /*
                 * We don't want a hard dependency on Redis to allow traffic. Make sure to set
                 * an alert so you know if this is happening too much. Stripe's observed
                 * failure rate is 0.01%.
                 */
                logger.error("Error determining if user allowed from redis");
            }

            return Mono.just(new Response(true, getHeaders(routeConfig, -1L)));
        }).map(response -> {
            if (response.isAllowed()) {
                return response;
            } else {
                logger.error(String.format(RATE_LIMIT_EXCEED_ERROR_LOG_MSG, routeId, id));
                throw new ApiGatewayException(ErrorCode.ERR_1005);
            }
        });
    }

    /**
     * check demo request is allowed or not by rate limit
     *
     * @param routeId
     * @param id
     * @return
     */
    private Mono<Response> isAllowedForIPRateLimit(String routeId, String id) {
      //  String userKey = routeId + UNDERSCORE + id;
        Config routeConfig = Optional.ofNullable(
                Optional.ofNullable(getConfig().getOrDefault(routeId, defaultConfig))
                        .orElse(getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS))
        ).orElseThrow(() -> new IllegalArgumentException(String.format("No Configuration found for route %s or defaultFilters", routeId)));
        int rateLimit = ipRateLimit;
        String key = getCurrentDayKey(id);
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        LocalDate now = LocalDate.now();
                        LocalDate tomorrow = now.plusDays(1);
                        Duration duration = Duration.between(now.atStartOfDay(), tomorrow.atStartOfDay());
                        redisTemplate.expire(key, duration).subscribe();
                    }
                    if (count > rateLimit) {
                        logger.warn(RATE_LIMIT_EXCEED_ERROR_FOR_IP_LOG_MSG, routeId, id.replaceFirst(IP_BASED_RATE_LIMIT_PREFIX, ""));
                        throw new ApiGatewayException(ErrorCode.ERR_1005);
                    } else {
                        return Mono.just(new Response(true, getHeaders(routeConfig, rateLimit - count)));
                    }
                });
    }

    /**
     * get a key for current day with demo rate limit key
     *
     * @param userKey
     * @return
     */
    private String getCurrentDayKey(String userKey) {
        LocalDate today = LocalDate.now();
        return userKey + ":" + today;
    }

    static List<String> getKeys(String id) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = "request_rate_limiter.{" + id;

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public Map<String, String> getHeaders(Config config, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        headers.put(REMAINING_HEADER, tokensLeft.toString());
        headers.put(REPLENISH_RATE_HEADER, String.valueOf(config.getReplenishRate()));
        headers.put(BURST_CAPACITY_HEADER, String.valueOf(config.getBurstCapacity()));
        headers.put(REQUESTED_TOKENS_HEADER, String.valueOf(config.getRequestedTokens()));
        return headers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (initialized.compareAndSet(false, true)) {
            if (this.redisTemplate == null) {
                this.redisTemplate = context.getBean(ReactiveStringRedisTemplate.class);
            }
            this.script = context.getBean(REDIS_SCRIPT_NAME, RedisScript.class);
            if (context.getBeanNamesForType(ConfigurationService.class).length > 0) {
                setConfigurationService(context.getBean(ConfigurationService.class));
            }
        }
    }

    public static class Config extends RedisRateLimiter.Config {
        /**
         * Put common value for this property in config to share the rate limit among multiple APIs
         */
        String groupId;

        /**
         * Put demp api rate limit
         */

        private int demoRateLimit = 0;

        public String getGroupId() {
            return groupId;
        }

        public Config setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public int getDemoRateLimit() {
            return demoRateLimit;
        }

        public void setDemoRateLimit(int demoRateLimit) {
            this.demoRateLimit = demoRateLimit;
        }

        public static Config of(int replenishRate, int burstCapacity, int requestedTokens) {
            Config config = new Config();
            config.setReplenishRate(replenishRate);
            config.setBurstCapacity(burstCapacity);
            config.setRequestedTokens(requestedTokens);
            return config;
        }

    }
}
