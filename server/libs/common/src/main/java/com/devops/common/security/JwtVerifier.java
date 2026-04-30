package com.devops.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

/**
 * Read-only JWT utility — verifies and parses tokens issued by auth-service.
 * Not a Spring component; instantiate it explicitly as a @Bean in each
 * service's
 * SecurityConfig to avoid a name conflict with auth-service's token-generation
 * service.
 */
public class JwtVerifier {

    private final SecretKey signingKey;

    public JwtVerifier(String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public String extractEmail(String token) {
        return (String) parseToken(token).get("email");
    }

    public String extractRole(String token) {
        return (String) parseToken(token).get("role");
    }
}
