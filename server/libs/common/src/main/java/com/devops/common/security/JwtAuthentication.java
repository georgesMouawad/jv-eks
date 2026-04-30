package com.devops.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

/**
 * Represents an authenticated principal derived from a validated JWT.
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final UUID userId;
    private final String email;

    public JwtAuthentication(UUID userId, String email, String role) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userId = userId;
        this.email = email;
        setAuthenticated(true);
    }

    /**
     * Returns the userId string so that {@code auth.getName()} works in
     * controllers.
     */
    @Override
    public String getName() {
        return userId.toString();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
