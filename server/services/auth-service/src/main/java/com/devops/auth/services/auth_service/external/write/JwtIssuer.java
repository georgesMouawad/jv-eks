package com.devops.auth.services.auth_service.external.write;

import com.devops.auth.services.auth_service.core.domain.AppUser;

public interface JwtIssuer {

    String generateToken(AppUser user);

    long getExpirationMs();
}
