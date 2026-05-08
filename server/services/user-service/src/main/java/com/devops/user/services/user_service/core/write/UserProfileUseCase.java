package com.devops.user.services.user_service.core.write;

import com.devops.user.services.user_service.presentation.write.dto.CreateUserProfileRequest;
import com.devops.user.services.user_service.presentation.write.dto.UpdateUserProfileRequest;
import com.devops.user.services.user_service.presentation.read.dto.UserProfileResponse;

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
