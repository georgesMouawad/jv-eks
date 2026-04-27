package com.devops.user.services.user_service.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(min = 1, max = 50, message = "First name cannot exceed 50 characters")
        String firstName,

        @Size(min = 1, max = 50, message = "Last name cannot exceed 50 characters")
        String lastName,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio
) {}
