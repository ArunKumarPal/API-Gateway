package com.arun.sample.gateway.filter;

import com.arun.sample.gateway.config.JwkAuthProperties;
import com.arun.sample.gateway.constants.ErrorCode;
import com.arun.sample.gateway.exception.TokenValidatorException;
import com.arun.sample.gateway.model.JwkAuthInfo;
import com.arun.sample.gateway.model.Pair;
import com.auth0.jwt.exceptions.JWTDecodeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.arun.sample.gateway.constants.Constants.AUTH_VALIDATOR_HEADER_KEY;

@Component
public class TokenValidationFilter extends AbstractGatewayFilterFactory<Object> {
    public static final int TOKEN_VALIDATION_FILTER_ORDER = 1;
    private static final Log logger = LogFactory.getLog(TokenValidationFilter.class);
    private final TokenValidationHandler tokenValidationHandler;
    private final ServerBearerTokenAuthenticationConverter converter;
    private final Map<String, JwkAuthInfo> authenticationManagerMap;

    @Autowired
    public TokenValidationFilter(
            TokenValidationHandler tokenValidationHandler,
            JwkAuthProperties jwkAuthProperties
    ) {
        super(Object.class);

        this.tokenValidationHandler = tokenValidationHandler;
        authenticationManagerMap = jwkAuthProperties.getJwt().entrySet().stream()
                .map(kv -> Pair.of(kv.getKey().toLowerCase(), tokenValidationHandler.parse(kv.getValue())))
                .collect(Collectors.toMap(Pair::t1, Pair::t2));

        converter = new ServerBearerTokenAuthenticationConverter();
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return this.converter.convert(exchange)
                .switchIfEmpty(Mono.error(new InvalidBearerTokenException("Unable to parse JWT token")))
                .flatMap(token -> authenticate(exchange, chain, token))
                .onErrorResume(AuthenticationException.class, ex -> {
                    logger.error("Error In Token Authentication  ", ex);
                    return Mono.error(new TokenValidatorException(ErrorCode.ERR_1001));
                })
                .onErrorResume(JWTDecodeException.class, ex -> {
                    logger.error("Error In Token Authentication  ", ex);
                    return Mono.error(new TokenValidatorException(ErrorCode.ERR_1001));
                })
                .doFinally(signalType -> MDC.clear());
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, GatewayFilterChain chain, Authentication token) {
        String authValidatorHeader = exchange.getRequest().getHeaders().getFirst(AUTH_VALIDATOR_HEADER_KEY);
        JwkAuthInfo jwkAuthInfo = tokenValidationHandler.getCorrectJwkInfo(authValidatorHeader, authenticationManagerMap);
        return jwkAuthInfo.authManager().authenticate(token)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.error(new ProviderNotFoundException("No provider found for " + token.getClass()))))
                .flatMap(auth -> validateClaims(auth, jwkAuthInfo, exchange))
                .flatMap(authentication -> chain.filter(exchange))
                .doOnError(AuthenticationException.class, ex -> {
                    logger.error("Authentication failed for workspace:".formatted("tokenInfo.workspaceId()"), ex);
                });
    }

    private Mono<Authentication> validateClaims(Authentication token, JwkAuthInfo jwkAuthInfo, ServerWebExchange exchange) {
        Optional<Jwt> optionalJwt = Optional.ofNullable((Jwt) token.getCredentials());
        boolean isIssuerCorrect = optionalJwt.map(creds -> creds.getIssuer().toString()).orElse("").equals(jwkAuthInfo.claimValidatorIssuer());
        boolean isAudienceCorrect = optionalJwt.map(JwtClaimAccessor::getAudience).orElse(List.of()).contains(jwkAuthInfo.claimValidatorAudience());
        if (isIssuerCorrect && isAudienceCorrect) {
            return Mono.just(token);
        } else {
            String errorMsg = String.format("Unable to validate claims. Issuer mismatch: %s, Audience mismatch: %s", !isIssuerCorrect, !isAudienceCorrect);
            logger.error(errorMsg);
            return Mono.error(new AuthenticationServiceException(errorMsg));
        }
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter(this::filter, TOKEN_VALIDATION_FILTER_ORDER);
    }
}

@Component
class TokenValidationHandler {
    private final Log logger = LogFactory.getLog(this.getClass());

    public JwkAuthInfo parse(Object value) {
        Map<String, Object> valueMap = (Map<String, Object>) value;
        String jwkUrl = valueMap.get("jwk-set-url").toString();
        Map<String, String> claimValidatorData = (Map<String, String>) valueMap.get("claims-validators");
        String claimValidatorIssuer = claimValidatorData.get("issuer");
        String claimValidatorAudience = claimValidatorData.get("audience");
        JwtReactiveAuthenticationManager authManager = new JwtReactiveAuthenticationManager(new NimbusReactiveJwtDecoder(jwkUrl));
        return new JwkAuthInfo(claimValidatorIssuer, claimValidatorAudience, authManager);
    }

    public JwkAuthInfo getCorrectJwkInfo(String authValidatorHeader, Map<String, JwkAuthInfo> authenticationManagerMap) {
        return Optional.ofNullable(authValidatorHeader)
                .map(String::toLowerCase)
                .map(authenticationManagerMap::get)
                .orElse(authenticationManagerMap.get("default"));
    }
}
