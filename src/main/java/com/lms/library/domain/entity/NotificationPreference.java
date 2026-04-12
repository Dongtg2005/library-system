package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    
    private Long userId;
    private String notificationType;
    private Boolean enabled;
    private NotificationChannel channel;
    
    public enum NotificationChannel {
        EMAIL, PUSH, SMS, IN_APP
    }
    
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
