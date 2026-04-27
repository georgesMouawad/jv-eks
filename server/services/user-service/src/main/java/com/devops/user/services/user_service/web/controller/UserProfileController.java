package com.devops.user.services.user_service.web.controller;

import com.devops.user.services.user_service.application.dto.CreateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UpdateUserProfileRequest;
import com.devops.user.services.user_service.application.dto.UserProfileResponse;
import com.devops.user.services.user_service.application.port.in.UserProfileUseCase;
import com.devops.user.services.user_service.infrastructure.security.JwtAuthentication;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileUseCase userProfileUseCase;

    public UserProfileController(UserProfileUseCase userProfileUseCase) {
        this.userProfileUseCase = userProfileUseCase;
    }

    /**
     * Create a profile for the authenticated user (called once after registration).
     */
    @PostMapping("/profile")
    public ResponseEntity<UserProfileResponse> createProfile(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody CreateUserProfileRequest request) {

        UserProfileResponse response = userProfileUseCase.createProfile(
                auth.getUserId(), auth.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get the authenticated user's own profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal JwtAuthentication auth) {

        return ResponseEntity.ok(userProfileUseCase.getProfile(auth.getUserId()));
    }

    /**
     * Get any user's profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileUseCase.getProfile(id));
    }

    /**
     * Update the authenticated user's own profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal JwtAuthentication auth,
            @Valid @RequestBody UpdateUserProfileRequest request) {

        return ResponseEntity.ok(
                userProfileUseCase.updateProfile(auth.getUserId(), auth.getUserId(), request));
    }

    /**
     * Delete the authenticated user's own profile.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyProfile(@AuthenticationPrincipal JwtAuthentication auth) {
        userProfileUseCase.deleteProfile(auth.getUserId(), auth.getUserId());
        return ResponseEntity.noContent().build();
    }
}
