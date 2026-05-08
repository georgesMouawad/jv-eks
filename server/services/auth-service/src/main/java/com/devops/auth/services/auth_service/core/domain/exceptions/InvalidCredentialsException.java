package com.devops.auth.services.auth_service.core.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }
}
