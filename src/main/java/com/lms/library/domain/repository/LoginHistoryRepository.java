package com.lms.library.domain.repository;

import com.lms.library.domain.entity.LoginHistory;
import com.lms.library.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    long countByUserAndStatusAndLoginAtAfter(
            User user,
            LoginHistory.LoginStatus status,
            LocalDateTime loginAtAfter
    );

    Page<LoginHistory> findByUserIdOrderByLoginAtDesc(Long userId, Pageable pageable);
}
