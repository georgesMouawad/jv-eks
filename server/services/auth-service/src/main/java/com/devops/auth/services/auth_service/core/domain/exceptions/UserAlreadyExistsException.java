package com.devops.auth.services.auth_service.core.domain.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String field, String value) {
        super("User with " + field + " '" + value + "' already exists.");
    }
}
