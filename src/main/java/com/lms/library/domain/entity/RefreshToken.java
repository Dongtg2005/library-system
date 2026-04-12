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
public class RefreshToken {
    
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private Boolean revoked;
    private LocalDateTime createdAt;
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !Boolean.TRUE.equals(revoked) && !isExpired();
    }
    
    public void revoke() {
        this.revoked = true;
    }
}
