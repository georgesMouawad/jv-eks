package com.devops.auth.services.auth_service.infrastructure.persistence;

import com.devops.auth.services.auth_service.domain.model.AppUser;
import com.devops.auth.services.auth_service.domain.repository.AppUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter: implements the domain repository port using Spring Data JPA.
 */
@Repository
public class UserPersistenceAdapter implements AppUserRepository {

    private final SpringDataUserRepository jpaRepository;

    public UserPersistenceAdapter(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public AppUser save(AppUser user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }
}
