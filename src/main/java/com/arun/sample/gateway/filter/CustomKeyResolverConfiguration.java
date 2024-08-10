package com.arun.sample.gateway.filter;

import com.arun.sample.gateway.constants.ErrorCode;
import com.arun.sample.gateway.exception.ApiGatewayException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.RequestPath;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.arun.sample.gateway.constants.Constants.*;

@Configuration
public class CustomKeyResolverConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CustomKeyResolverConfiguration.class);

    @Bean
    @Primary
    KeyResolver userKeyResolver() {
        return exchange ->
                isIpRateLimitApiCall(Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(SERVICE_NAME_HEADER)))
                        ? getIpRateLimitKey(exchange) : getRateLimitKey(exchange);
    }

    private Mono<String> getIpRateLimitKey(ServerWebExchange exchange) {
        return Optional.ofNullable(getClientIP(exchange))
                .map(this::getIpRateLimitKey)
                .map(Mono::just)
                .orElse(Mono.error(new ApiGatewayException(ErrorCode.ERR_1004)));
    }

    private String getClientIP(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(X_FORWARDED_FOR))
                .orElseGet(() -> Optional.ofNullable(exchange.getRequest().getRemoteAddress()).map(remote -> remote.getAddress().getHostAddress()).orElse(null));
    }

    private Mono<String> getRateLimitKey(ServerWebExchange exchange) {
        return getKey(exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER), exchange.getRequest().getPath())
                .map(Mono::just)
                .orElse(Mono.error(new ApiGatewayException(ErrorCode.ERR_1004)));
    }

    private boolean isIpRateLimitApiCall(Optional<String> serviceHeader) {
        return serviceHeader
                .map(header -> header.equalsIgnoreCase(IP_BASED_RATE_LIMIT_HEADER_VALUE))
                .orElseGet(() -> Boolean.FALSE);
    }

    private String getIpRateLimitKey(String ipAddress) {
        return IP_BASED_RATE_LIMIT_PREFIX + ipAddress;
    }

    private Optional<String> getKey(String token, RequestPath requestPath) {
        return Optional.ofNullable(token).flatMap(x -> {
            try {
                DecodedJWT decodedToken = JWT.decode(x.substring(7));
                String workSpaceId = decodedToken.getClaim("username").toString().replace("\"", "");
                return Optional.of(workSpaceId);
            } catch (Exception e) {
                logger.error("Error in getting key for rate limiting in API path - {}", requestPath, e);
                return Optional.empty();
            }
        });
    }
}
