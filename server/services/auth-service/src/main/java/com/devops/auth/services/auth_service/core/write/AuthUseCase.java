package com.devops.auth.services.auth_service.core.write;

import com.devops.auth.services.auth_service.presentation.read.dto.AuthResponse;
import com.devops.auth.services.auth_service.presentation.write.dto.LoginRequest;
import com.devops.auth.services.auth_service.presentation.write.dto.RegisterRequest;

/**
 * Input port: defines the use-cases exposed by the application layer.
 */
public interface AuthUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
