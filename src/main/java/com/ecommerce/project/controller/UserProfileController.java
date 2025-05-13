package com.ecommerce.project.controller;

import com.ecommerce.project.payload.UserProfileDTO;
import com.ecommerce.project.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.project.model.UserProfileDTO;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        UserProfileDTO profile = userProfileService.getUserProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> createUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDTO userProfileDto) {
        UserProfileDTO createdProfile = userProfileService.createUserProfile(userId, userProfileDto);
        return ResponseEntity.ok(createdProfile);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDTO userProfileDto) {
        UserProfileDTO updatedProfile = userProfileService.updateUserProfile(userId, userProfileDto);
        return ResponseEntity.ok(updatedProfile);
    }
}