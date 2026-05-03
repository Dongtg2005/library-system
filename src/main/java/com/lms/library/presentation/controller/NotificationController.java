package com.lms.library.presentation.controller;

import com.lms.library.application.dto.NotificationDTO;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.NotificationService;
import com.lms.library.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    private final AuthenticationService authenticationService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications() {
        Long userId = getCurrentUserId();
        log.info("Fetching notifications for user: {}", userId);
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<Page<NotificationDTO>> getUserNotificationsPaged(Pageable pageable) {
        Long userId = getCurrentUserId();
        log.info("Fetching paged notifications for user: {}", userId);
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        Long userId = getCurrentUserId();
        log.info("Fetching unread notifications for user: {}", userId);
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Marking notification as read: {} for user: {}", id, userId);
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<Void> deleteAllNotifications() {
        log.info("Deleting all notifications for user");
        notificationService.deleteAllNotifications();
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        String email = ControllerHelper.getCurrentUserEmail();
        User user = authenticationService.findByEmail(email);
        return user.getId();
    }
}
