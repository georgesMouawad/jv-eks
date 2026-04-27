package com.devops.user.services.user_service.infrastructure.persistence;

import com.devops.user.services.user_service.domain.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataUserProfileRepository extends JpaRepository<UserProfile, UUID> {}
