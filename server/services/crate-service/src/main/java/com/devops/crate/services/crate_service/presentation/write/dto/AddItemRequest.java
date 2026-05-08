package com.devops.crate.services.crate_service.presentation.write.dto;

import jakarta.validation.constraints.NotBlank;

public record AddItemRequest(@NotBlank String trackName, @NotBlank String s3Key) {
}
