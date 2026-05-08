package com.devops.auth.services.auth_service.presentation.read.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String email,
        Set<String> authorities,
        String token,
        long expiresIn
) {}
