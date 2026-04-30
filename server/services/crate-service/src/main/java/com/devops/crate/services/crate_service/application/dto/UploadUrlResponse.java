package com.devops.crate.services.crate_service.application.dto;

public record UploadUrlResponse(
        String uploadUrl,
        String s3Key,
        long expiresInSeconds) {
}
