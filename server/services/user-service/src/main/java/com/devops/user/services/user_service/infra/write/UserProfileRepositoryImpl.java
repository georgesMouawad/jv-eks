package com.devops.user.services.user_service.infra.write;

import com.devops.user.services.user_service.core.domain.UserProfile;
import com.devops.user.services.user_service.core.read.UserProfileRepository;
import com.devops.user.services.user_service.infra.read.SpringDataUserProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements the domain repository port using Spring Data JPA.
 */
@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final SpringDataUserProfileRepository jpaRepository;

    public UserProfileRepositoryImpl(SpringDataUserProfileRepository jpaRepository) {
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
