package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(nullable = false)
    private Boolean read = false;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "push_sent")
    private Boolean pushSent = false;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (read == null) read = false;
        if (emailSent == null) emailSent = false;
        if (pushSent == null) pushSent = false;
    }

    public enum NotificationType {
        // User notifications
        DUE_SOON, OVERDUE, AVAILABLE, RESERVED, APPROVED, REJECTED,
        // Librarian notifications
        NEW_BORROW_REQUEST, OVERDUE_REMINDER, BOOK_RETURNED,
        // Admin notifications
        SYSTEM_ALERT, REPORT_GENERATED
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
