package com.devops.crate.services.crate_service.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CrateResponse(
        UUID id,
        String name,
        UUID ownerId,
        LocalDateTime createdAt,
        List<CrateItemResponse> items) {
}
