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
    
    public static AuthResponse from(User user, String token, LocalDateTime expiresAt) {
        String roleName = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().get(0).getName()
                : "USER";
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .tokenType("Bearer")
                .accessToken(token)
                .expiresAt(expiresAt)
                .build();
    }
}
