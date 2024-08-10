package com.arun.sample.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CustomRouteDefinitionLocator implements RouteDefinitionLocator {

    private static final Log logger = LogFactory.getLog(CustomRouteDefinitionLocator.class);

    private final String folder;

    public CustomRouteDefinitionLocator (@Value("${routes.folder}") String folder) {
        this.folder = folder;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:routes/" + folder + "/*.yml");

            return Flux.fromIterable(Arrays.stream(resources)
                    .map(resource -> new YamlRouteDefinitionReader(resource).read())
                    .flatMap(List :: stream)
                    .toList());

        } catch (Exception e) {
            logger.error("Exception while loading routes yml ", e);
            return Flux.just();
        }
    }
}
