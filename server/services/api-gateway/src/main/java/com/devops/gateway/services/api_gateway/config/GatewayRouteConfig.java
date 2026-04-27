package com.devops.gateway.services.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Configuration
public class GatewayRouteConfig {

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri(URI.create(authServiceUrl)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/users/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri(URI.create(userServiceUrl)))
                .build();
    }
}
