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
        return buildAuthResponse(user, java.math.BigDecimal.ZERO);
    }
    
    public static AuthResponse buildAuthResponse(User user, java.math.BigDecimal outstandingFines) {
        String roleName = "USER";
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            if (user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
                roleName = "ADMIN";
            } else if (user.getRoles().stream().anyMatch(role -> "LIBRARIAN".equalsIgnoreCase(role.getName()))) {
                roleName = "LIBRARIAN";
            }
        }
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .tokenType("Bearer")
                .outstandingFines(outstandingFines != null ? outstandingFines : java.math.BigDecimal.ZERO)
                .hasOutstandingFines(outstandingFines != null && outstandingFines.compareTo(java.math.BigDecimal.ZERO) > 0)
                .build();
    }
}
