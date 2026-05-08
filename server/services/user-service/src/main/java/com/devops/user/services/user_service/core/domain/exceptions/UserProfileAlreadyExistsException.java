package com.devops.user.services.user_service.core.domain.exceptions;

public class UserProfileAlreadyExistsException extends RuntimeException {

    public UserProfileAlreadyExistsException() {
        super("A profile for this user already exists.");
    }
}
