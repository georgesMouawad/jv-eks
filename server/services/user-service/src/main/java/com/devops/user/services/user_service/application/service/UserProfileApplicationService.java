package com.devops.user.services.user_service.application.service;

import com.devops.user.services.user_service.application.dto.CreateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UpdateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UserProfileResponse;
import com.devops.user.services.user_service.application.port.in.UserProfileUseCase;
import com.devops.user.services.user_service.domain.exception.UnauthorizedException;
import com.devops.user.services.user_service.domain.exception.UserProfileAlreadyExistsException;
import com.devops.user.services.user_service.domain.exception.UserProfileNotFoundException;
import com.devops.user.services.user_service.domain.model.UserProfile;
import com.devops.user.services.user_service.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserProfileApplicationService implements UserProfileUseCase {

    private final UserProfileRepository profileRepository;

    public UserProfileApplicationService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public UserProfileResponse createProfile(UUID userId, String email, CreateUserProfileRequest request) {
        if (profileRepository.existsById(userId)) {
            throw new UserProfileAlreadyExistsException();
        }

        UserProfile profile = UserProfile.create(userId, request.firstName(), request.lastName(), email);
        if (request.bio() != null) {
            profile.updateDetails(request.firstName(), request.lastName(), request.bio());
        }

        return UserProfileResponse.from(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return profileRepository.findById(userId)
                .map(UserProfileResponse::from)
                .orElseThrow(() -> new UserProfileNotFoundException(userId));
    }

    @Override
    public UserProfileResponse updateProfile(UUID requesterId, UUID targetUserId, UpdateUserProfileRequest request) {
        if (!requesterId.equals(targetUserId)) {
            throw new UnauthorizedException();
        }

        UserProfile profile = profileRepository.findById(targetUserId)
                .orElseThrow(() -> new UserProfileNotFoundException(targetUserId));

        profile.updateDetails(request.firstName(), request.lastName(), request.bio());
        return UserProfileResponse.from(profileRepository.save(profile));
    }

    @Override
    public void deleteProfile(UUID requesterId, UUID targetUserId) {
        if (!requesterId.equals(targetUserId)) {
            throw new UnauthorizedException();
        }

        if (!profileRepository.existsById(targetUserId)) {
            throw new UserProfileNotFoundException(targetUserId);
        }

        profileRepository.deleteById(targetUserId);
    }
}
