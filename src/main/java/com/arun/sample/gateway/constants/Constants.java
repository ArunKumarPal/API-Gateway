package com.arun.sample.gateway.constants;

public class Constants {
    private Constants(){}
    public static final String AUTH_VALIDATOR_HEADER_KEY = "X-AUTH-VALIDATOR";
    public static final String USER_REGEX = ".*\"user\":\"(\\w+)\".*";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String IP_BASED_RATE_LIMIT_HEADER_VALUE = "IP_RATE_LIMIT";
    public static final String IP_BASED_RATE_LIMIT_PREFIX = "IP_RATE_LIMIT_";
    public static final String RATE_LIMIT_EXCEED_ERROR_LOG_MSG = "Rate Limit Exceeded for route id %s for user %s";
    public static final String RATE_LIMIT_EXCEED_ERROR_FOR_IP_LOG_MSG = "Rate Limit Exceeded of api call for route id {} for ip {}";

}
