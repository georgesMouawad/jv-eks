package com.devops.sync.services.sync_service.web.config;

import com.devops.sync.services.sync_service.web.handler.SyncWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    // Map /ws/sync/** to the SyncWebSocketHandler.
    // The handler extracts the crateId from the last path segment at runtime.
    // Order 1 ensures this mapping takes priority over the default WebFlux mapping.
    @Bean
    public HandlerMapping webSocketHandlerMapping(SyncWebSocketHandler handler) {
        Map<String, WebSocketHandler> urlMap = Map.of("/ws/sync/**", handler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping(urlMap, 1);
        return mapping;
    }

    // Required adapter that bridges the WebFlux DispatcherHandler to
    // WebSocketHandler
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
