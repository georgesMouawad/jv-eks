package com.devops.user.services.user_service.domain.repository;

import com.devops.user.services.user_service.domain.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port: persistence contract decoupled from any framework.
 */
public interface UserProfileRepository {

    UserProfile save(UserProfile profile);

    Optional<UserProfile> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
