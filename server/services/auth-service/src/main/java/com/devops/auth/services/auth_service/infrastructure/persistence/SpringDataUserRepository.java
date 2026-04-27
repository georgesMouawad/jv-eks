package com.devops.auth.services.auth_service.infrastructure.persistence;

import com.devops.auth.services.auth_service.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);
}
