package com.devops.auth.services.auth_service.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

@Embeddable
public class Authority implements GrantedAuthority {

    @Column(name = "authority", nullable = false, length = 50)
    private String authority;

    protected Authority() {
    }

    private Authority(String authority) {
        this.authority = authority;
    }

    public static Authority of(String authority) {
        return new Authority(authority);
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authority that)) {
            return false;
        }
        return Objects.equals(authority, that.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority);
    }
}