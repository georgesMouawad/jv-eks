package com.devops.user.services.user_service.domain.exception;

import java.util.UUID;

public class UserProfileNotFoundException extends RuntimeException {

    public UserProfileNotFoundException(UUID id) {
        super("User profile not found for id: " + id);
    }
}
