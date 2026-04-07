package com.lms.library.auth.service;

import com.lms.library.auth.dto.LoginRequest;
import com.lms.library.auth.dto.AuthResponse;
import com.lms.library.auth.dto.RegisterRequest;
import com.lms.library.auth.entity.User;
import com.lms.library.auth.exception.EmailAlreadyExistsException;
import com.lms.library.auth.exception.InvalidCredentialsException;
import com.lms.library.auth.repository.UserRepository;
import com.lms.library.auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * Đăng ký người dùng mới
     */
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        // Tạo user mới
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Lưu user vào database
        userRepository.save(user);
        
        // Tạo JWT token
        String token = jwtUtil.generateToken(user);
        
        // Trả về response
        return AuthResponse.from(user, token, jwtUtil.getExpirationTime());
    }
    
    /**
     * Đăng nhập người dùng
     */
    public AuthResponse login(LoginRequest request) {
        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);
        
        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        // Kiểm tra tài khoản có bị vô hiệu hóa
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Tài khoản đã bị vô hiệu hóa");
        }
        
        // Tạo JWT token
        String token = jwtUtil.generateToken(user);
        
        // Trả về response
        return AuthResponse.from(user, token, jwtUtil.getExpirationTime());
    }
    
    /**
     * Xác thực người dùng từ JWT token
     */
    @Transactional(readOnly = true)
    public User validateToken(String token) {
        try {
            String email = jwtUtil.extractEmail(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("Người dùng không tồn tại"));
        } catch (InvalidCredentialsException e) {
            throw e;  // Re-throw application exceptions
        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Token đã hết hạn");
        } catch (JwtException e) {
            throw new InvalidCredentialsException("Token không hợp lệ");
        } catch (Exception e) {
            throw new InvalidCredentialsException("Lỗi xác thực token");
        }
    }
    
    /**
     * Tìm user theo email
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Người dùng không tồn tại"));
    }
}
