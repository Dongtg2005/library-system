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
public class Notification {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private ResourceType resourceType;
    private UUID resourceId;
    private Boolean read;
    private Boolean emailSent;
    private Boolean pushSent;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        DUE_SOON, OVERDUE, AVAILABLE, RESERVED, 
        APPROVED, REJECTED, SYSTEM_ALERT
    }
    
    public enum ResourceType {
        BOOK, USER, REVIEW, RESERVATION, FINE
    }
    
    public boolean isUnread() {
        return read == null || !read;
    }
    
    public boolean isBookRelated() {
        return ResourceType.BOOK.equals(resourceType);
    }
    
    public boolean isUrgent() {
        return NotificationType.OVERDUE.equals(type) || 
               NotificationType.SYSTEM_ALERT.equals(type);
    }
}
