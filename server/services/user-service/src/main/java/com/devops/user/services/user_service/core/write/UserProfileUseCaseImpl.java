package com.devops.user.services.user_service.core.write;

import com.devops.user.services.user_service.presentation.write.dto.CreateUserProfileRequest;
import com.devops.user.services.user_service.presentation.write.dto.UpdateUserProfileRequest;
import com.devops.user.services.user_service.presentation.read.dto.UserProfileResponse;
import com.devops.user.services.user_service.core.domain.exceptions.UnauthorizedException;
import com.devops.user.services.user_service.core.domain.exceptions.UserProfileAlreadyExistsException;
import com.devops.user.services.user_service.core.domain.exceptions.UserProfileNotFoundException;
import com.devops.user.services.user_service.core.domain.UserProfile;
import com.devops.user.services.user_service.core.read.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserProfileUseCaseImpl implements UserProfileUseCase {

    private final UserProfileRepository profileRepository;

    public UserProfileUseCaseImpl(UserProfileRepository profileRepository) {
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
