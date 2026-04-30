package com.devops.crate.services.crate_service.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crate_items")
public class CrateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID crateId;

    @Column(nullable = false, length = 200)
    private String trackName;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private UUID addedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    protected CrateItem() {
    }

    public static CrateItem create(UUID crateId, String trackName, String s3Key, UUID addedBy) {
        CrateItem item = new CrateItem();
        item.crateId = crateId;
        item.trackName = trackName;
        item.s3Key = s3Key;
        item.addedBy = addedBy;
        return item;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCrateId() {
        return crateId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getS3Key() {
        return s3Key;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
