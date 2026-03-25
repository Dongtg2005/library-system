package com.lms.library.auth;

import com.lms.library.auth.dto.LoginRequest;
import com.lms.library.auth.dto.RegisterRequest;
import com.lms.library.auth.entity.User;
import com.lms.library.auth.repository.UserRepository;
import com.lms.library.auth.service.AuthenticationService;
import com.lms.library.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yaml")
public class AuthenticationServiceTest {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    public void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
    }
    
    @Test
    public void testUserRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();
        
        var response = authenticationService.register(request);
        
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertNotNull(response.getAccessToken());
    }
    
    @Test
    public void testUserLogin() {
        // First register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login@example.com")
                .password("password123")
                .fullName("Login Test User")
                .build();
        authenticationService.register(registerRequest);
        
        // Then login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@example.com")
                .password("password123")
                .build();
        
        var response = authenticationService.login(loginRequest);
        
        assertNotNull(response);
        assertEquals("login@example.com", response.getEmail());
        assertNotNull(response.getAccessToken());
    }
    
    @Test
    public void testInvalidCredentials() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("invalid@example.com")
                .password("password123")
                .fullName("Invalid Test User")
                .build();
        authenticationService.register(registerRequest);
        
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid@example.com")
                .password("wrongpassword")
                .build();
        
        assertThrows(Exception.class, () -> authenticationService.login(loginRequest));
    }
}
