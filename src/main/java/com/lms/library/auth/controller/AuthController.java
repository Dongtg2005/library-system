package com.lms.library.auth.controller;

import com.lms.library.auth.dto.LoginRequest;
import com.lms.library.auth.dto.AuthResponse;
import com.lms.library.auth.dto.RegisterRequest;
import com.lms.library.auth.entity.User;
import com.lms.library.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    
    /**
     * Đăng ký người dùng mới
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Yêu cầu đăng ký cho email: {}", request.getEmail());
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Đăng nhập người dùng
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Yêu cầu đăng nhập cho email: {}", request.getEmail());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thông tin user hiện tại
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = authenticationService.findByEmail(email);
        log.info("Lấy thông tin user: {}", email);
        
        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Xác thực token
     * GET /api/auth/validate?token=xxx
     */
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        log.info("Xác thực token");
        User user = authenticationService.validateToken(token);
        
        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đăng xuất người dùng
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        log.info("Người dùng đã đăng xuất");
        return ResponseEntity.noContent().build();
    }
}
