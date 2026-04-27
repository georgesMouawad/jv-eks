package com.devops.user.services.user_service.application.port.in;

import com.devops.user.services.user_service.application.dto.CreateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UpdateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UserProfileResponse;

import java.util.UUID;

/**
 * Input port: use-cases available to the web adapter.
 */
public interface UserProfileUseCase {

    UserProfileResponse createProfile(UUID userId, String email, CreateUserProfileRequest request);

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID requesterId, UUID targetUserId, UpdateUserProfileRequest request);

    void deleteProfile(UUID requesterId, UUID targetUserId);
}
