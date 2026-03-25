package com.lms.library.auth.dto;

import com.lms.library.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    
    public static AuthResponse from(User user, String token, Long expiresIn) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
