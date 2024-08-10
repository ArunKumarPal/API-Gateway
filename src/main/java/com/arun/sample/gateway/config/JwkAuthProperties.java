package com.arun.sample.gateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "token")
public class JwkAuthProperties {
    private Map<String, Object> jwt;
}