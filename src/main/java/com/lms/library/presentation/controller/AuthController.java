package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.LoginHistoryService;
import com.lms.library.application.service.RefreshTokenService;
import com.lms.library.domain.entity.LoginHistory;
import com.lms.library.domain.entity.RefreshToken;
import com.lms.library.domain.entity.User;
import com.lms.library.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Operations related to user authentication")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryService loginHistoryService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        String email = ControllerHelper.getCurrentUserEmail();

        if (email == null || "anonymousUser".equals(email)) {
            log.warn("Ngăn chặn lỗi NPE: Attempted to get /me without valid authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = authenticationService.findByEmail(email);
        java.math.BigDecimal outstandingFines = authenticationService.getOutstandingFines(user.getId());
        AuthResponse response = ControllerHelper.buildAuthResponse(user, outstandingFines);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Validating token from Header");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        try {
            User user = authenticationService.validateToken(token);
            AuthResponse response = ControllerHelper.buildAuthResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Token expired or invalid"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        log.info("Requesting access token rotation");
        String oldRefreshToken = body.get("refreshToken");
        if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token không được để trống"));
        }

        try {
            RefreshToken rotatedToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);
            String newAccessToken = jwtUtil.generateToken(rotatedToken.getUser());
            return ResponseEntity.ok(Map.of(
                    "token", newAccessToken,
                    "refreshToken", rotatedToken.getToken()
            ));
        } catch (Exception e) {
            log.error("Token rotation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> body) {
        log.info("Requesting logout for specific token");
        if (body != null && body.containsKey("refreshToken")) {
            try {
                String tokenStr = body.get("refreshToken");
                RefreshToken token = refreshTokenService.verifyRefreshToken(tokenStr);
                token.setRevoked(true);
            } catch (Exception ignored) {}
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutAll() {
        String email = ControllerHelper.getCurrentUserEmail();
        log.info("Requesting logout-all for user: {}", email);
        User user = authenticationService.findByEmail(email);
        refreshTokenService.revokeAllUserTokens(user.getId());
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Đăng xuất tất cả thiết bị thành công"));
    }

    @GetMapping("/login-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> loginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = ControllerHelper.getCurrentUserEmail();
        log.info("Fetching login history for user: {}", email);
        User user = authenticationService.findByEmail(email);
        Page<LoginHistory> history = loginHistoryService.getLoginHistory(user.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(history);
    }
}
