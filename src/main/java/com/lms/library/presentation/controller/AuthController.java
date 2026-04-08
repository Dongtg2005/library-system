package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
        User user = authenticationService.findByEmail(email);
        log.info("Getting current user info: {}", email);
        
        AuthResponse response = ControllerHelper.buildAuthResponse(user);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        log.info("Validating token");
        User user = authenticationService.validateToken(token);
        
        AuthResponse response = ControllerHelper.buildAuthResponse(user);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
        return ResponseEntity.noContent().build();
    }
}
