package com.lms.library.application.service;

import com.lms.library.domain.entity.LoginHistory;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.repository.LoginHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Transactional
    public void saveLogin(User user, HttpServletRequest request,
                          LoginHistory.LoginType type,
                          LoginHistory.LoginStatus status) {
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(xHeader -> xHeader.split(",")[0].trim())
                .orElse(request.getRemoteAddr());

        String userAgent = request.getHeader("User-Agent");

        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(ip)
                .userAgent(userAgent)
                .loginAt(LocalDateTime.now())
                .loginType(type)
                .status(status)
                .location("Local Network") // Có thể tích hợp GeoIP ở đây nếu cần
                .build();

        loginHistoryRepository.save(history);

        // Phát hiện bất thường: nếu là FAILED và có > 5 lần FAILED trong 1 giờ
        if (status == LoginHistory.LoginStatus.FAILED) {
            long failedCount = loginHistoryRepository
                    .countByUserAndStatusAndLoginAtAfter(
                            user,
                            LoginHistory.LoginStatus.FAILED,
                            LocalDateTime.now().minusHours(1)
                    );

            if (failedCount > 5) {
                log.warn("Phát hiện đăng nhập bất thường: user={}, ip={}", user.getEmail(), ip);
                LoginHistory suspiciousHistory = LoginHistory.builder()
                        .user(user)
                        .ipAddress(ip)
                        .userAgent(userAgent)
                        .loginAt(LocalDateTime.now())
                        .loginType(type)
                        .status(LoginHistory.LoginStatus.SUSPICIOUS)
                        .location("Local Network")
                        .build();
                loginHistoryRepository.save(suspiciousHistory);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<LoginHistory> getLoginHistory(Long userId, Pageable pageable) {
        return loginHistoryRepository.findByUserIdOrderByLoginAtDesc(userId, pageable);
    }
}
