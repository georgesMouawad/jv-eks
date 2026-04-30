package com.devops.crate.services.crate_service.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCrateRequest(@NotBlank String name) {
}
