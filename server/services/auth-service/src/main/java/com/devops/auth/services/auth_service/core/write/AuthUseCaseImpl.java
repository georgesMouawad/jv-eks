package com.devops.auth.services.auth_service.core.write;

import com.devops.auth.services.auth_service.presentation.read.dto.AuthResponse;
import com.devops.auth.services.auth_service.presentation.write.dto.LoginRequest;
import com.devops.auth.services.auth_service.presentation.write.dto.RegisterRequest;
import com.devops.auth.services.auth_service.core.domain.AppUser;
import com.devops.auth.services.auth_service.core.domain.exceptions.InvalidCredentialsException;
import com.devops.auth.services.auth_service.core.domain.exceptions.UserAlreadyExistsException;
import com.devops.auth.services.auth_service.core.read.AppUserRepository;
import com.devops.auth.services.auth_service.external.write.JwtIssuer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthUseCaseImpl implements AuthUseCase {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthUseCaseImpl(AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtIssuer jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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
                passwordEncoder.encode(request.password()));

        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        Set<String> authorities = saved.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new AuthResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                authorities,
                token,
                jwtService.getExpirationMs());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidCredentialsException();
        }

        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        String token = jwtService.generateToken(user);
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                authorities,
                token,
                jwtService.getExpirationMs());
    }
}
