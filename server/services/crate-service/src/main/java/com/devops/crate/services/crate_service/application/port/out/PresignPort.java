package com.devops.crate.services.crate_service.application.port.out;

import java.util.UUID;

public interface PresignPort {
    String generatePresignedPutUrl(String s3Key, long expiresInSeconds);

    String generateS3Key(UUID crateId, String trackName);
}
