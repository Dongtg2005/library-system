package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.BorrowManagementService;
import com.lms.library.domain.entity.BorrowPolicy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
@Slf4j
public class BorrowController {
    
    private final BorrowManagementService borrowManagementService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<BorrowResponse> createBorrowing(
            @Valid @RequestBody CreateBorrowRequest request,
            @RequestParam(required = false, defaultValue = "USER") BorrowPolicy.MemberType memberType) {
        
        UUID memberId = ControllerHelper.getCurrentUserId();
        log.info("Creating borrow request for member: {}", memberId);
        BorrowResponse response = borrowManagementService.createBorrowing(memberId, memberType, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<ReturnResponse> processReturn(@Valid @RequestBody ReturnRequest request) {
        UUID memberId = ControllerHelper.getCurrentUserId();
        log.info("Processing return for member: {}", memberId);
        ReturnResponse response = borrowManagementService.processReturn(memberId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{borrowRecordId}/extend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<BorrowResponse> extendLoan(@PathVariable UUID borrowRecordId) {
        UUID memberId = ControllerHelper.getCurrentUserId();
        log.info("Extending loan for record: {}", borrowRecordId);
        BorrowResponse response = borrowManagementService.extendLoan(borrowRecordId, memberId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<List<BorrowResponse>> getMemberBorrowHistory() {
        UUID memberId = ControllerHelper.getCurrentUserId();
        log.info("Getting borrow history for member: {}", memberId);
        List<BorrowResponse> response = borrowManagementService.getMemberBorrowHistory(memberId);
        return ResponseEntity.ok(response);
    }
    
    // Admin endpoints for managing all borrows
    @GetMapping("/admin/{memberId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getMemberBorrowHistoryAdmin(@PathVariable UUID memberId) {
        log.info("Getting borrow history for member: {} (admin access)", memberId);
        List<BorrowResponse> response = borrowManagementService.getMemberBorrowHistory(memberId);
        return ResponseEntity.ok(response);
    }
}
