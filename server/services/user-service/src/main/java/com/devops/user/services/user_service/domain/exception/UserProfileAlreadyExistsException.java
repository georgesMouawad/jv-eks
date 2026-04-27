package com.devops.user.services.user_service.domain.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {

    public UserProfileAlreadyExistsException() {
        super("A profile for this user already exists.");
    }
}
