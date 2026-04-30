package com.devops.sync.services.sync_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class RedisConfig {

    // ReactiveStringRedisTemplate is a ReactiveRedisTemplate<String, String>
    // pre-configured with String serializers. It exposes listenToChannel() which
    // returns a Flux<Message<String, String>> — exactly what the WebSocket handler
    // needs to pipe Redis messages to connected clients.
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }
}
