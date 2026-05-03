package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    long countByUserIdAndReadFalse(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND n.read = false AND n.type IN ?2 ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdAndTypes(Long userId, List<Notification.NotificationType> types);
}
