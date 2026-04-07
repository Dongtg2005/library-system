package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN,
        LIBRARIAN,
        USER
    }
    
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }
    
    public boolean isLibrarian() {
        return Role.LIBRARIAN.equals(this.role);
    }
    
    public boolean isRegularUser() {
        return Role.USER.equals(this.role);
    }
    
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }
}
