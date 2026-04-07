package com.lms.library.presentation.controller;

import com.lms.library.application.dto.AuthResponse;
import com.lms.library.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class ControllerHelper {
    
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            // Fallback for email-based authentication
            throw new IllegalStateException("Invalid user ID format in authentication context");
        }
    }
    
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    public static AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
    }
}
