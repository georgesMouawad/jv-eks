package com.devops.crate.services.crate_service.infrastructure.messaging;

import com.devops.crate.services.crate_service.application.port.out.CrateEventPublisher;
import com.devops.crate.services.crate_service.domain.model.CrateItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class RedisEventPublisher implements CrateEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisEventPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishTrackAdded(UUID crateId, CrateItem item) {
        // Channel name matches what the sync-service subscribes to:
        // crate-updates-<crateId>
        String channel = "crate-updates-" + crateId;
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "crateId", crateId.toString(),
                    "itemId", item.getId().toString(),
                    "trackName", item.getTrackName(),
                    "addedBy", item.getAddedBy().toString(),
                    "addedAt", item.getAddedAt().toString()));
            redisTemplate.convertAndSend(channel, payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize crate event", e);
        }
    }
}
