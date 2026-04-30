package com.devops.sync.services.sync_service.web.handler;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SyncWebSocketHandler implements WebSocketHandler {

    private final ReactiveStringRedisTemplate redisTemplate;

    public SyncWebSocketHandler(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Connection URL pattern: /ws/sync/{crateId}
    // The crateId is the last path segment.
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        String crateId = path.substring(path.lastIndexOf('/') + 1);

        // Subscribe to the Redis Pub/Sub channel for this crate.
        // Each message published by the crate-service (RedisEventPublisher)
        // is forwarded as a WebSocket text frame to this client.
        // The Flux completes (and the WebSocket closes) when the Redis
        // subscription is cancelled or the connection is dropped.
        Flux<WebSocketMessage> outbound = redisTemplate
                .listenToChannel("crate-updates-" + crateId)
                .map(message -> session.textMessage(message.getMessage()));

        return session.send(outbound);
    }
}
