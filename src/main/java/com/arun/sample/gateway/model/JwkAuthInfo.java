package com.arun.sample.gateway.model;

import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;

public record JwkAuthInfo(String claimValidatorIssuer, String claimValidatorAudience, JwtReactiveAuthenticationManager authManager) {
}
