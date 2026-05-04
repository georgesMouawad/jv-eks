package com.devops.crate.services.crate_service.application.dto;

import com.devops.crate.services.crate_service.domain.model.Crate;
import com.devops.crate.services.crate_service.domain.model.CrateItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CrateResponse(
        UUID id,
        String name,
        UUID ownerId,
        LocalDateTime createdAt,
        List<CrateItemResponse> items) {

    public static CrateResponse from(Crate crate, List<CrateItem> items) {
        return new CrateResponse(
                crate.getId(),
                crate.getName(),
                crate.getOwnerId(),
                crate.getCreatedAt(),
                items.stream().map(CrateItemResponse::from).toList());
    }
}
