package com.lms.library.user.controller;

import com.lms.library.user.dto.CreateUserRequest;
import com.lms.library.user.dto.UpdateRoleRequest;
import com.lms.library.user.dto.UpdateStatusRequest;
import com.lms.library.user.dto.UpdateUserRequest;
import com.lms.library.user.dto.UserResponse;
import com.lms.library.user.entity.UserProfile;
import com.lms.library.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                   @RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id,
                                                @RequestHeader("X-Auth-User-Id") Long requesterAuthUserId,
                                                @RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.ok(userService.getById(id, requesterAuthUserId, role));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll(@RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.ok(userService.getAll(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserRequest request,
                                                      @RequestHeader("X-Auth-User-Id") Long requesterAuthUserId,
                                                      @RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.ok(userService.updateProfile(id, request, requesterAuthUserId, role));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoleRequest request,
                                                   @RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.ok(userService.updateRole(id, request, role));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateStatusRequest request,
                                                     @RequestHeader("X-User-Role") UserProfile.Role role) {
        return ResponseEntity.ok(userService.updateStatus(id, request, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader("X-User-Role") UserProfile.Role role) {
        userService.deleteUser(id, role);
        return ResponseEntity.noContent().build();
    }
}
