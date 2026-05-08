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
import com.lms.library.domain.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Page<BorrowResponse>> getAllBorrows(
            @RequestParam(required = false) BorrowRecord.BorrowStatus status,
            Pageable pageable) {
        log.info("Librarian/Admin requesting all borrows with status: {}", status);
        Page<BorrowResponse> response = borrowManagementService.getAllBorrows(status, pageable);
        return ResponseEntity.ok(response);
    }
    
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<ReturnResponse> processReturn(@Valid @RequestBody ReturnRequest request) {
        Long memberId = getCurrentUserId();
        log.info("Processing return for librarian: {}", memberId);
        ReturnResponse response = borrowManagementService.processReturnByLibrarian(request);
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

    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<BorrowResponse> checkBorrowStatus(@RequestParam UUID bookId) {
        Long memberId = getCurrentUserId();
        log.info("Checking borrow status for member: {} and book: {}", memberId, bookId);
        BorrowResponse response = borrowManagementService.checkBorrowStatus(memberId, bookId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
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

    // Librarian endpoints for approving/rejecting borrow requests
    @PutMapping("/{borrowRecordId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowResponse> approveBorrowRequest(@PathVariable UUID borrowRecordId) {
        Long librarianId = getCurrentUserId();
        log.info("Approving borrow request: {} by librarian: {}", borrowRecordId, librarianId);
        BorrowResponse response = borrowManagementService.approveBorrowRequest(borrowRecordId, librarianId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{borrowRecordId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowResponse> rejectBorrowRequest(
            @PathVariable UUID borrowRecordId,
            @RequestParam(required = false) String reason) {
        Long librarianId = getCurrentUserId();
        log.info("Rejecting borrow request: {} by librarian: {} with reason: {}", borrowRecordId, librarianId, reason);
        BorrowResponse response = borrowManagementService.rejectBorrowRequest(borrowRecordId, librarianId, reason);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = ControllerHelper.getCurrentUserEmail();
        User user = authenticationService.findByEmail(email);
        return user.getId();
    }
}
