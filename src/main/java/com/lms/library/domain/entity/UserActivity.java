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
public class UserActivity {
    private Long id;
    private Long userId;
    private ActivityType activityType;
    private ResourceType resourceType;
    private String resourceId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    
    public enum ActivityType {
        LOGIN, LOGOUT, BORROW, RETURN, REVIEW, 
        SEARCH, VIEW_BOOK, ADD_FAVORITE, 
        RESERVE_BOOK, CANCEL_RESERVATION
    }
    
    public enum ResourceType {
        BOOK, USER, REVIEW, RESERVATION, CATEGORY, TAG
    }
    
    public boolean isBookRelated() {
        return ResourceType.BOOK.equals(resourceType) || 
               ActivityType.BORROW.equals(activityType) ||
               ActivityType.RETURN.equals(activityType) ||
               ActivityType.VIEW_BOOK.equals(activityType);
    }
}
