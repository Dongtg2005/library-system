package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {
    
    private Long id;
    private String eventType;
    private Long userId;
    private String sessionId;
    private Map<String, Object> properties;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    
    // Common event types
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String EVENT_LOGOUT = "LOGOUT";
    public static final String EVENT_BORROW = "BORROW";
    public static final String EVENT_RETURN = "RETURN";
    public static final String EVENT_SEARCH = "SEARCH";
    public static final String EVENT_VIEW_BOOK = "VIEW_BOOK";
    public static final String EVENT_ADD_FAVORITE = "ADD_FAVORITE";
    public static final String EVENT_REVIEW = "REVIEW";
    public static final String EVENT_RESERVE = "RESERVE";
    
    public boolean hasProperty(String key) {
        return properties != null && properties.containsKey(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return properties != null ? (T) properties.get(key) : null;
    }
}
