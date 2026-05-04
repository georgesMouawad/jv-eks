package com.devops.crate.services.crate_service.application.dto;

import com.devops.crate.services.crate_service.domain.model.CrateItem;

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
