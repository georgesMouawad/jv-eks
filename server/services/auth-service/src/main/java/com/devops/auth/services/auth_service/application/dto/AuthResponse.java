package com.devops.auth.services.auth_service.application.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String email,
        String role,
        String token,
        long expiresIn
) {}
