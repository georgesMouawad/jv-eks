package com.devops.crate.services.crate_service.domain.exception;

import java.util.UUID;

public class CrateNotFoundException extends RuntimeException {

    public CrateNotFoundException(UUID id) {
        super("Crate not found: " + id);
    }
}
