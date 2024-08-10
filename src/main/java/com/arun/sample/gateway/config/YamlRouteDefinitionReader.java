package com.arun.sample.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class YamlRouteDefinitionReader {

    private static final Log logger = LogFactory.getLog(YamlRouteDefinitionReader.class);

    private final Resource resource;

    public YamlRouteDefinitionReader(Resource resource) {
        this.resource = resource;
    }

    public List<RouteDefinition> read(){
        Yaml yaml = new Yaml();
        try (InputStream inputStream = resource.getInputStream()) {
            return Arrays.asList(yaml.loadAs(inputStream, RouteDefinition[].class));
        } catch (IOException e) {
            logger.error("exception while reading route file :- ", e);
            throw new RuntimeException(e);
        }
    }
}