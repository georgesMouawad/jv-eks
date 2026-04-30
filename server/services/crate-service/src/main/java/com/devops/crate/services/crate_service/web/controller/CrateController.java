package com.devops.crate.services.crate_service.web.controller;

import com.devops.crate.services.crate_service.application.dto.*;
import com.devops.crate.services.crate_service.application.port.in.CrateUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/crates")
public class CrateController {

    private final CrateUseCase crateUseCase;

    public CrateController(CrateUseCase crateUseCase) {
        this.crateUseCase = crateUseCase;
    }

    // POST /api/crates — create a new shared crate owned by the authenticated user
    @PostMapping
    public ResponseEntity<CrateResponse> createCrate(@Valid @RequestBody CreateCrateRequest request,
            Authentication auth) {
        UUID ownerId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(crateUseCase.createCrate(request, ownerId));
    }

    // GET /api/crates/{id} — fetch crate metadata and its items
    @GetMapping("/{id}")
    public ResponseEntity<CrateResponse> getCrate(@PathVariable UUID id) {
        return ResponseEntity.ok(crateUseCase.getCrate(id));
    }

    // GET /api/crates/{id}/upload-url — generate a 15-minute pre-signed S3 PUT URL
    // The client uploads the file directly to S3, then calls POST /{id}/items to
    // confirm.
    @GetMapping("/{id}/upload-url")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(@PathVariable UUID id,
            @RequestParam String trackName) {
        return ResponseEntity.ok(crateUseCase.getUploadUrl(id, trackName));
    }

    // POST /api/crates/{id}/items — confirm upload and save track metadata.
    // Called by the frontend after a successful S3 PUT. Publishes to Redis so the
    // sync-service can broadcast the new track to all connected WebSocket clients.
    @PostMapping("/{id}/items")
    public ResponseEntity<CrateItemResponse> addItem(@PathVariable UUID id,
            @Valid @RequestBody AddItemRequest request,
            Authentication auth) {
        UUID addedBy = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(crateUseCase.addItem(id, request, addedBy));
    }
}
