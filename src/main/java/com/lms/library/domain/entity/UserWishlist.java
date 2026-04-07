package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWishlist {
    private Long id;
    private Long userId;
    private UUID bookId;
    private Integer priority; // 1=Low, 2=Medium, 3=High
    private Boolean notificationSent;
    private LocalDateTime createdAt;
    
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);
        
        private final int value;
        
        Priority(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public boolean isHighPriority() {
        return Priority.HIGH.getValue() == priority;
    }
    
    public boolean needsNotification() {
        return notificationSent == null || !notificationSent;
    }
}
