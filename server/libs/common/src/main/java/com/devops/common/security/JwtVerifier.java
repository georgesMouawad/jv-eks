package com.devops.common.security;

import io.jsonwebtoken.Claims;

import java.util.Collection;
import java.util.UUID;

public interface JwtVerifier {

    Claims parseToken(String token);

    boolean isValid(String token);

    UUID extractUserId(String token);

    String extractEmail(String token);

    Collection<String> extractAuthorities(String token);
}
