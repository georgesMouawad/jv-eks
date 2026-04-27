package com.devops.auth.services.auth_service.application.port.in;

import com.devops.auth.services.auth_service.application.dto.AuthResponse;
import com.devops.auth.services.auth_service.application.dto.LoginRequest;
import com.devops.auth.services.auth_service.application.dto.RegisterRequest;

/**
 * Input port: defines the use-cases exposed by the application layer.
 */
public interface AuthUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
