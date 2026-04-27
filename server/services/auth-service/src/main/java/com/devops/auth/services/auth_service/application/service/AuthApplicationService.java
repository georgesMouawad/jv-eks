package com.devops.auth.services.auth_service.application.service;

import com.devops.auth.services.auth_service.application.dto.AuthResponse;
import com.devops.auth.services.auth_service.application.dto.LoginRequest;
import com.devops.auth.services.auth_service.application.dto.RegisterRequest;
import com.devops.auth.services.auth_service.application.port.in.AuthUseCase;
import com.devops.auth.services.auth_service.domain.exception.InvalidCredentialsException;
import com.devops.auth.services.auth_service.domain.exception.UserAlreadyExistsException;
import com.devops.auth.services.auth_service.domain.model.AppUser;
import com.devops.auth.services.auth_service.domain.model.Role;
import com.devops.auth.services.auth_service.domain.repository.AppUserRepository;
import com.devops.auth.services.auth_service.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthApplicationService implements AuthUseCase {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthApplicationService(AppUserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("email", request.email());
        }

        AppUser user = AppUser.create(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole().name(),
                token,
                jwtService.getExpirationMs()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                token,
                jwtService.getExpirationMs()
        );
    }
}
