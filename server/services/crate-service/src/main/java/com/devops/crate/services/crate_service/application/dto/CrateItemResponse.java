package com.devops.crate.services.crate_service.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrateItemResponse(
        UUID id,
        String trackName,
        String s3Key,
        UUID addedBy,
        LocalDateTime addedAt) {
}
