package com.lms.library.application.service;

import com.lms.library.application.dto.AuthResponse;
import com.lms.library.application.dto.LoginRequest;
import com.lms.library.application.dto.RegisterRequest;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.repository.UserRepository;
import com.lms.library.infrastructure.security.JwtUtil;
import com.lms.library.domain.exception.EmailAlreadyExistsException;
import com.lms.library.domain.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserManagementService userManagementService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(User.UserStatus.ACTIVE)
            .roles(new ArrayList<>(java.util.List.of(userManagementService.resolveRole("USER"))))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser);
        
        return AuthResponse.from(savedUser, token, jwtUtil.getExpirationTime());
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Logging in user with email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is disabled");
        }
        
        String token = jwtUtil.generateToken(user);
        return AuthResponse.from(user, token, jwtUtil.getExpirationTime());
    }
    
    @Transactional(readOnly = true)
    public User validateToken(String token) {
        try {
            String email = jwtUtil.extractEmail(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }
    
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }
}
