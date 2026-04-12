package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.BorrowManagementService;
import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrows")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "Borrow Management", description = "Operations related to book borrowing and returning")
public class BorrowController {
    
    private final BorrowManagementService borrowManagementService;
    private final AuthenticationService authenticationService;
    
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new borrowing record", 
                                             description = "Accepts a book borrowing request, validates policy, and creates a record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Borrowing process initiated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or borrowing limit exceeded")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<BorrowResponse> createBorrowing(
            @Valid @RequestBody CreateBorrowRequest request,
            @RequestParam(required = false, defaultValue = "USER") BorrowPolicy.MemberType memberType) {
        
        Long memberId = getCurrentUserId();
        log.info("Creating borrow request for member: {}", memberId);
        BorrowResponse response = borrowManagementService.createBorrowing(memberId, memberType, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<ReturnResponse> processReturn(@Valid @RequestBody ReturnRequest request) {
        Long memberId = getCurrentUserId();
        log.info("Processing return for member: {}", memberId);
        ReturnResponse response = borrowManagementService.processReturn(memberId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{borrowRecordId}/extend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<BorrowResponse> extendLoan(@PathVariable UUID borrowRecordId) {
        Long memberId = getCurrentUserId();
        log.info("Extending loan for record: {}", borrowRecordId);
        BorrowResponse response = borrowManagementService.extendLoan(borrowRecordId, memberId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<List<BorrowResponse>> getMemberBorrowHistory() {
        Long memberId = getCurrentUserId();
        log.info("Getting borrow history for member: {}", memberId);
        List<BorrowResponse> response = borrowManagementService.getMemberBorrowHistory(memberId);
        return ResponseEntity.ok(response);
    }
    
    // Admin endpoints for managing all borrows
    @GetMapping("/admin/{memberId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getMemberBorrowHistoryAdmin(@PathVariable Long memberId) {
        log.info("Getting borrow history for member: {} (admin access)", memberId);
        List<BorrowResponse> response = borrowManagementService.getMemberBorrowHistory(memberId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = ControllerHelper.getCurrentUserEmail();
        User user = authenticationService.findByEmail(email);
        return user.getId();
    }
}
