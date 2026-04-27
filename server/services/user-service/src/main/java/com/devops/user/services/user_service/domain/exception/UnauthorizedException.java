package com.devops.user.services.user_service.domain.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("You are not authorized to perform this action.");
    }
}
