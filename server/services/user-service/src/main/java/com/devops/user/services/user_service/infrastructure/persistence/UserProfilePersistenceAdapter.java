package com.devops.user.services.user_service.infrastructure.persistence;

import com.devops.user.services.user_service.domain.model.UserProfile;
import com.devops.user.services.user_service.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements the domain repository port using Spring Data JPA.
 */
@Repository
public class UserProfilePersistenceAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository jpaRepository;

    public UserProfilePersistenceAdapter(SpringDataUserProfileRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return jpaRepository.save(profile);
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
