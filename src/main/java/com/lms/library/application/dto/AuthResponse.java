package com.lms.library.application.dto;

import com.lms.library.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String tokenType;
    private String accessToken;
    private LocalDateTime expiresAt;

    private java.math.BigDecimal outstandingFines = java.math.BigDecimal.ZERO;
    private Boolean hasOutstandingFines = false;

    private static String resolveRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "USER";
        }

        if (user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
            return "ADMIN";
        }

        if (user.getRoles().stream().anyMatch(role -> "LIBRARIAN".equalsIgnoreCase(role.getName()))) {
            return "LIBRARIAN";
        }

        return "USER";
    }
    
    public static AuthResponse from(User user, String token, LocalDateTime expiresAt) {
        return from(user, token, expiresAt, java.math.BigDecimal.ZERO);
    }

    public static AuthResponse from(User user, String token, LocalDateTime expiresAt, java.math.BigDecimal outstandingFines) {
        String roleName = resolveRole(user);
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .tokenType("Bearer")
                .accessToken(token)
                .expiresAt(expiresAt)
                .outstandingFines(outstandingFines != null ? outstandingFines : java.math.BigDecimal.ZERO)
                .hasOutstandingFines(outstandingFines != null && outstandingFines.compareTo(java.math.BigDecimal.ZERO) > 0)
                .build();
    }
}
