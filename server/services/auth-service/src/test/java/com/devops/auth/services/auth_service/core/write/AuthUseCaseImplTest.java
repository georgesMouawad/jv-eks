package com.devops.auth.services.auth_service.core.write;

import com.devops.auth.services.auth_service.core.domain.AppUser;
import com.devops.auth.services.auth_service.core.domain.exceptions.InvalidCredentialsException;
import com.devops.auth.services.auth_service.core.domain.exceptions.UserAlreadyExistsException;
import com.devops.auth.services.auth_service.core.read.AppUserRepository;
import com.devops.auth.services.auth_service.external.write.JwtIssuer;
import com.devops.auth.services.auth_service.presentation.write.dto.LoginRequest;
import com.devops.auth.services.auth_service.presentation.write.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthUseCaseImpl.class)
class AuthUseCaseImplTest {

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtIssuer jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthUseCase authUseCase;

    // ── register helpers ─────────────────────────────────────────────────────

    private AppUser stubRegisterSuccess() {
        AppUser saved = AppUser.create("john", "john@example.com", "hashed");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        return saved;
    }

    private static final RegisterRequest REGISTER_REQUEST = new RegisterRequest("john", "john@example.com", "secret");

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_success_returnsUsername() {
        stubRegisterSuccess();
        assertThat(authUseCase.register(REGISTER_REQUEST).username()).isEqualTo("john");
    }

    @Test
    void register_success_returnsEmail() {
        stubRegisterSuccess();
        assertThat(authUseCase.register(REGISTER_REQUEST).email()).isEqualTo("john@example.com");
    }

    @Test
    void register_success_returnsJwtToken() {
        stubRegisterSuccess();
        assertThat(authUseCase.register(REGISTER_REQUEST).token()).isEqualTo("jwt-token");
    }

    @Test
    void register_success_returnsExpiry() {
        stubRegisterSuccess();
        assertThat(authUseCase.register(REGISTER_REQUEST).expiresIn()).isEqualTo(3600000L);
    }

    @Test
    void register_success_assignsDefaultRole() {
        stubRegisterSuccess();
        assertThat(authUseCase.register(REGISTER_REQUEST).authorities()).contains("ROLE_USER");
    }

    @Test
    void register_success_persistsUser() {
        stubRegisterSuccess();
        authUseCase.register(REGISTER_REQUEST);
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void register_throwsWhenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertThatThrownBy(() -> authUseCase.register(REGISTER_REQUEST))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThatThrownBy(() -> authUseCase.register(REGISTER_REQUEST))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsUsername() {
        AppUser user = AppUser.create("john", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        assertThat(authUseCase.login(new LoginRequest("john@example.com", "secret")).username())
                .isEqualTo("john");
    }

    @Test
    void login_success_returnsEmail() {
        AppUser user = AppUser.create("john", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        assertThat(authUseCase.login(new LoginRequest("john@example.com", "secret")).email())
                .isEqualTo("john@example.com");
    }

    @Test
    void login_success_returnsJwtToken() {
        AppUser user = AppUser.create("john", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        assertThat(authUseCase.login(new LoginRequest("john@example.com", "secret")).token())
                .isEqualTo("jwt-token");
    }

    @Test
    void login_success_invokesAuthenticationManager() {
        AppUser user = AppUser.create("john", "john@example.com", "hashed");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        authUseCase.login(new LoginRequest("john@example.com", "secret"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_throwsOnBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));
        assertThatThrownBy(() -> authUseCase.login(new LoginRequest("john@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsWhenUserNotFoundAfterAuthentication() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.login(new LoginRequest("ghost@example.com", "secret")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
