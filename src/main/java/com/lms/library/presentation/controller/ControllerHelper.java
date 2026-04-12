package com.lms.library.presentation.controller;

import com.lms.library.application.dto.AuthResponse;
import com.lms.library.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ControllerHelper {
    
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ignored) {
            // Current JWT subject is email, so derive deterministic UUID from email
            return UUID.nameUUIDFromBytes(principal.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    public static AuthResponse buildAuthResponse(User user) {
        String roleName = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().get(0).getName()
                : "USER";
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .tokenType("Bearer")
                .build();
    }
}
