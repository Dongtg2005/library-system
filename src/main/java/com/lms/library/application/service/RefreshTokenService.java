package com.lms.library.application.service;

import com.lms.library.domain.entity.RefreshToken;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpiration; // tính bằng mili-giây (ví dụ: 604800000)

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo) {
        log.info("Creating new refresh token for user: {}", user.getEmail());
        return refreshTokenRepository.save(RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .revoked(false)
                .deviceInfo(deviceInfo)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        return token;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenStr) {
        log.info("Rotating refresh token");
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        if (oldToken.isRevoked() || oldToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token đã hết hạn hoặc bị thu hồi");
        }

        // Thu hồi token cũ
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Tạo token mới
        return createRefreshToken(oldToken.getUser(), oldToken.getDeviceInfo());
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        log.info("Revoking all refresh tokens for user ID: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
