package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        
        // Thêm check an toàn chống NullPointerException nếu có lọt filter
        if (email == null || "anonymousUser".equals(email)) {
            log.warn("Ngăn chặn lỗi NPE: Attempted to get /me without valid authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Fix: Lấy User object từ email trước khi build response
        User user = authenticationService.findByEmail(email);
        java.math.BigDecimal outstandingFines = authenticationService.getOutstandingFines(user.getId());
        AuthResponse response = ControllerHelper.buildAuthResponse(user, outstandingFines);
        return ResponseEntity.ok(response);
    }
    
// Fix 2.6: Security REST convention -> DO NOT expose token as a query param in URL
    // Client MUST send: Header: Authorization: Bearer <token>
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Validating token from Header");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        
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
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
        return ResponseEntity.noContent().build();
    }
}
