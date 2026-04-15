package com.lms.library.application.service;

import com.lms.library.application.dto.UserCreateRequest;
import com.lms.library.application.dto.UserResponse;
import com.lms.library.application.dto.UserUpdateRequest;
import com.lms.library.domain.entity.Role;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.exception.DuplicateResourceException;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.repository.RoleRepository;
import com.lms.library.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll(pageable).map(UserResponse::from);
        }

        return userRepository
                .findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(query.trim(), query.trim(), pageable)
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email [" + request.getEmail() + "] already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(resolveStatus(request.getStatus()))
            .roles(new ArrayList<>(List.of(resolveRole(request.getRole()))))
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String nextEmail = request.getEmail().trim();
            if (!nextEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(nextEmail)) {
                throw new DuplicateResourceException("User with email [" + nextEmail + "] already exists");
            }
            user.setEmail(nextEmail);
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(resolveStatus(request.getStatus()));
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRoles(new ArrayList<>(List.of(resolveRole(request.getRole()))));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
    }

    public Role resolveRole(String roleName) {
        String normalized = roleName == null || roleName.isBlank() ? Role.ROLE_USER : roleName.trim().toUpperCase();

        return roleRepository.findByName(normalized)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(normalized)
                        .description(normalized + " role")
                        .permissions(resolvePermissions(normalized))
                        .build()));
    }

    private User.UserStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return User.UserStatus.ACTIVE;
        }

        try {
            return User.UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return User.UserStatus.ACTIVE;
        }
    }

    private Map<String, Boolean> resolvePermissions(String roleName) {
        return switch (roleName) {
            case Role.ROLE_ADMIN -> Role.ADMIN_PERMISSIONS;
            case Role.ROLE_LIBRARIAN -> Role.LIBRARIAN_PERMISSIONS;
            case Role.ROLE_GUEST -> Role.GUEST_PERMISSIONS;
            default -> Role.USER_PERMISSIONS;
        };
    }
}
