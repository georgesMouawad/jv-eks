package com.devops.crate.services.crate_service.core.write;

import java.util.UUID;

public interface Presign {
    String generatePresignedPutUrl(String s3Key, long expiresInSeconds);

    String generateS3Key(UUID crateId, String trackName);
}
