package com.devops.user.services.user_service.application.dto;

import com.devops.user.services.user_service.domain.model.UserProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String bio,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getBio(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
