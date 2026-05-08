package com.devops.user.services.user_service.infra.read;

import com.devops.user.services.user_service.core.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataUserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
