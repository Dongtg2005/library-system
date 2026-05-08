package com.lms.library.application.service;

import com.lms.library.application.dto.NotificationDTO;
import com.lms.library.domain.entity.Notification;
import com.lms.library.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final com.lms.library.domain.repository.UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        log.info("Fetching notifications for user: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        log.info("Fetching notifications for user: {} with pagination", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for user: {}", userId);
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification as read: {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }
        
        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Transactional
    public NotificationDTO createNotification(Long userId, Notification.NotificationType type, 
                                              String title, String content, 
                                              Notification.ResourceType resourceType, 
                                              UUID resourceId) {
        log.info("Creating notification for user: {} type: {}", userId, type);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .read(false)
                .emailSent(false)
                .pushSent(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }
    
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications() {
        // Delete all notifications
        // This is a cleanup method to fix old notifications with wrong resource IDs
        notificationRepository.deleteAll();
        log.info("Deleted all notifications");
    }
    
    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .read(notification.getRead())
                .isUnread(notification.isUnread())
                .isUrgent(notification.isUrgent())
                .isBookRelated(notification.isBookRelated())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // User notifications
    public NotificationDTO notifyUserDueSoon(Long userId, String bookTitle, UUID bookId, LocalDateTime dueDate) {
        return createNotification(
            userId,
            Notification.NotificationType.DUE_SOON,
            "Sách sắp hết hạn",
            String.format("Sách \"%s\" sẽ hết hạn vào %s", bookTitle, dueDate.toLocalDate()),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    public NotificationDTO notifyUserOverdue(Long userId, String bookTitle, UUID bookId, int overdueDays) {
        return createNotification(
            userId,
            Notification.NotificationType.OVERDUE,
            "Sách quá hạn",
            String.format("Sách \"%s\" đã quá hạn %d ngày", bookTitle, overdueDays),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    public NotificationDTO notifyUserFineCreated(Long userId, java.math.BigDecimal amount, String bookTitle) {
        return createNotification(
            userId,
            Notification.NotificationType.OVERDUE,
            "Phí phạt trả sách muộn",
            String.format("Bạn có khoản phí phạt mới trị giá %,.0f VND cho cuốn sách \"%s\" do trả muộn.", amount, bookTitle),
            Notification.ResourceType.BOOK,
            null
        );
    }


    public NotificationDTO notifyUserBookAvailable(Long userId, String bookTitle, UUID bookId) {
        return createNotification(
            userId,
            Notification.NotificationType.AVAILABLE,
            "Sách có sẵn",
            String.format("Sách \"%s\" bạn đã đặt trước nay đã có sẵn", bookTitle),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    public NotificationDTO notifyUserApproved(Long userId, String bookTitle, UUID bookId) {
        return createNotification(
            userId,
            Notification.NotificationType.APPROVED,
            "Yêu cầu mượn được duyệt",
            String.format("Yêu cầu mượn sách \"%s\" đã được duyệt", bookTitle),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    public NotificationDTO notifyUserRejected(Long userId, String bookTitle, String reason) {
        return createNotification(
            userId,
            Notification.NotificationType.REJECTED,
            "Yêu cầu mượn bị từ chối",
            String.format("Yêu cầu mượn sách \"%s\" bị từ chối. Lý do: %s", bookTitle, reason),
            Notification.ResourceType.BOOK,
            null
        );
    }

    public NotificationDTO notifyUserBookOnHold(Long userId, String bookTitle, UUID bookId, int holdHours) {
        return createNotification(
            userId,
            Notification.NotificationType.AVAILABLE,
            "Sách đang giữ chỗ cho bạn",
            String.format("Sách \"%s\" đang được giữ chỗ. Bạn có %d giờ để đến thư viện lấy hoặc xác nhận mượn online. Sau %d giờ sẽ tự động hủy.", bookTitle, holdHours, holdHours),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    public NotificationDTO notifyUserHoldExpired(Long userId, String bookTitle, UUID bookId) {
        return createNotification(
            userId,
            Notification.NotificationType.EXPIRED,
            "Hết thời gian giữ chỗ",
            String.format("Bạn đã hết thời gian 48 giờ để lấy sách \"%s\". Sách sẽ được chuyển cho người tiếp theo trong danh sách chờ.", bookTitle),
            Notification.ResourceType.BOOK,
            bookId
        );
    }

    // Librarian notifications
    public void notifyLibrarianNewBorrowRequest(String userName, String bookTitle, UUID borrowId) {
        List<com.lms.library.domain.entity.User> librarians = userRepository.findByRoleName("LIBRARIAN");
        for (com.lms.library.domain.entity.User librarian : librarians) {
            createNotification(
                librarian.getId(),
                Notification.NotificationType.NEW_BORROW_REQUEST,
                "Yêu cầu mượn mới",
                String.format("Người dùng %s muốn mượn sách \"%s\"", userName, bookTitle),
                Notification.ResourceType.BOOK,
                borrowId
            );
        }
    }

    public void notifyLibrarianOverdueReminder(String userName, String bookTitle, int overdueDays) {
        List<com.lms.library.domain.entity.User> librarians = userRepository.findByRoleName("LIBRARIAN");
        for (com.lms.library.domain.entity.User librarian : librarians) {
            createNotification(
                librarian.getId(),
                Notification.NotificationType.OVERDUE_REMINDER,
                "Nhắc nhở quá hạn",
                String.format("Sách \"%s\" của %s đã quá hạn %d ngày", bookTitle, userName, overdueDays),
                Notification.ResourceType.BOOK,
                null
            );
        }
    }

    public void notifyLibrarianBookReturned(String userName, String bookTitle) {
        List<com.lms.library.domain.entity.User> librarians = userRepository.findByRoleName("LIBRARIAN");
        for (com.lms.library.domain.entity.User librarian : librarians) {
            createNotification(
                librarian.getId(),
                Notification.NotificationType.BOOK_RETURNED,
                "Sách đã được trả",
                String.format("%s đã trả sách \"%s\"", userName, bookTitle),
                Notification.ResourceType.BOOK,
                null
            );
        }
    }

    // Admin notifications
    public void notifyAdminSystemAlert(String title, String message) {
        List<com.lms.library.domain.entity.User> admins = userRepository.findByRoleName("ADMIN");
        for (com.lms.library.domain.entity.User admin : admins) {
            createNotification(
                admin.getId(),
                Notification.NotificationType.SYSTEM_ALERT,
                title,
                message,
                Notification.ResourceType.USER,
                null
            );
        }
    }

    public void notifyAdminReportGenerated(String reportType, String reportUrl) {
        List<com.lms.library.domain.entity.User> admins = userRepository.findByRoleName("ADMIN");
        for (com.lms.library.domain.entity.User admin : admins) {
            createNotification(
                admin.getId(),
                Notification.NotificationType.REPORT_GENERATED,
                "Báo cáo mới",
                String.format("Báo cáo %s đã được tạo. Xem tại: %s", reportType, reportUrl),
                Notification.ResourceType.USER,
                null
            );
        }
    }
}
