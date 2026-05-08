package com.devops.auth.services.auth_service.infra.write;

import com.devops.auth.services.auth_service.core.domain.AppUser;
import com.devops.auth.services.auth_service.core.read.AppUserRepository;
import com.devops.auth.services.auth_service.infra.read.SpringDataUserRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter: implements the domain repository port using Spring Data JPA.
 */
@Repository
public class AppUserRepositoryImpl implements AppUserRepository {

    private final SpringDataUserRepository jpaRepository;

    public AppUserRepositoryImpl(SpringDataUserRepository jpaRepository) {
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
