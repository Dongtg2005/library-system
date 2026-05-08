package com.lms.library.presentation.controller;

import com.lms.library.application.dto.FineResponse;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.FineService;
import com.lms.library.domain.entity.Fine;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "Fine Management", description = "Operations related to library fines and penalties")
public class FineController {

    private final FineService fineService;
    private final AuthenticationService authenticationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getAllFines() {
        log.info("Librarian/Admin requesting all fines");
        List<FineResponse> response = fineService.getAllFines();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<List<FineResponse>> getMyFines() {
        Long memberId = getCurrentUserId();
        log.info("Fetching fines for currently logged-in member: {}", memberId);
        List<FineResponse> response = fineService.getMemberFines(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getMemberFinesAdmin(@PathVariable Long memberId) {
        log.info("Librarian/Admin requesting fines for member: {}", memberId);
        List<FineResponse> response = fineService.getMemberFines(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getFinesByStatus(@RequestParam Fine.FineStatus status) {
        log.info("Librarian/Admin requesting fines with status: {}", status);
        List<FineResponse> response = fineService.getFinesByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fineId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<FineResponse> getFineById(@PathVariable UUID fineId) {
        log.info("Fetching fine details for ID: {}", fineId);
        FineResponse response = fineService.getFineById(fineId);
        
        // Security check: regular USER can only view their own fine
        Long currentUserId = getCurrentUserId();
        boolean isStaffOrAdmin = isStaffOrAdmin();
        if (!isStaffOrAdmin && !response.getMemberId().equals(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to view this fine.");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{fineId}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<FineResponse> payFine(@PathVariable UUID fineId) {
        log.info("Paying fine ID: {}", fineId);
        FineResponse response = fineService.getFineById(fineId);
        
        // Security check: regular USER can only pay their own fine
        Long currentUserId = getCurrentUserId();
        boolean isStaffOrAdmin = isStaffOrAdmin();
        if (!isStaffOrAdmin && !response.getMemberId().equals(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to pay this fine.");
        }
        
        FineResponse paidFine = fineService.payFine(fineId);
        return ResponseEntity.ok(paidFine);
    }

    @PostMapping("/{fineId}/waive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<FineResponse> waiveFine(@PathVariable UUID fineId) {
        log.info("Librarian/Admin waiving fine ID: {}", fineId);
        FineResponse response = fineService.waiveFine(fineId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{fineId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<FineResponse> cancelFine(@PathVariable UUID fineId) {
        log.info("Librarian/Admin cancelling fine ID: {}", fineId);
        FineResponse response = fineService.cancelFine(fineId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = ControllerHelper.getCurrentUserEmail();
        User user = authenticationService.findByEmail(email);
        return user.getId();
    }

    private boolean isStaffOrAdmin() {
        String email = ControllerHelper.getCurrentUserEmail();
        User user = authenticationService.findByEmail(email);
        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()) || "LIBRARIAN".equalsIgnoreCase(role.getName()));
    }
}
