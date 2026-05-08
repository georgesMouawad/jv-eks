package com.devops.crate.services.crate_service.presentation.read.dto;

public record UploadUrlResponse(
        String uploadUrl,
        String s3Key,
        long expiresInSeconds) {
}
