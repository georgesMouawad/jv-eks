package com.devops.crate.services.crate_service.presentation.read.dto;

import com.devops.crate.services.crate_service.core.domain.CrateItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrateItemResponse(
        UUID id,
        String trackName,
        String s3Key,
        UUID addedBy,
        LocalDateTime addedAt) {

    public static CrateItemResponse from(CrateItem item) {
        return new CrateItemResponse(
                item.getId(),
                item.getTrackName(),
                item.getS3Key(),
                item.getAddedBy(),
                item.getAddedAt());
    }
}
