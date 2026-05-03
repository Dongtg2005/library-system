package com.lms.library.application.dto;

import com.lms.library.domain.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private String title;
    private String content;
    private Notification.ResourceType resourceType;
    private UUID resourceId;
    private Boolean read;
    private Boolean isUnread;
    private Boolean isUrgent;
    private Boolean isBookRelated;
    private LocalDateTime createdAt;
}
