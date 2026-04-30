package com.devops.crate.services.crate_service.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crates")
public class Crate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Crate() {
    }

    public static Crate create(String name, UUID ownerId) {
        Crate crate = new Crate();
        crate.name = name;
        crate.ownerId = ownerId;
        return crate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
