package com.devops.auth.services.auth_service.domain.repository;

import com.devops.auth.services.auth_service.domain.model.AppUser;

import java.util.Optional;

/**
 * Output port: defines persistence contract without coupling to any framework.
 */
public interface AppUserRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    AppUser save(AppUser user);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);
}
