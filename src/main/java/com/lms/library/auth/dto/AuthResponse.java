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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private Long userId;
        private String email;
        private String fullName;
        private String role;
        private String accessToken;
        private String tokenType;
        private Long expiresIn;

        public AuthResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponseBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AuthResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public AuthResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AuthResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public AuthResponseBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.userId = this.userId;
            response.email = this.email;
            response.fullName = this.fullName;
            response.role = this.role;
            response.accessToken = this.accessToken;
            response.tokenType = this.tokenType;
            response.expiresIn = this.expiresIn;
            return response;
        }
    }
}
