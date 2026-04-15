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
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        String roleName = "USER";
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            if (user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
                roleName = "ADMIN";
            } else if (user.getRoles().stream().anyMatch(role -> "LIBRARIAN".equalsIgnoreCase(role.getName()))) {
                roleName = "LIBRARIAN";
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(roleName)
                .status(user.getStatus() != null ? user.getStatus().name() : User.UserStatus.ACTIVE.name())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
