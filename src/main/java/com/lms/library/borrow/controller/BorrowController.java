package com.lms.library.borrow.controller;

import com.lms.library.borrow.dto.BorrowResponse;
import com.lms.library.borrow.dto.CreateBorrowRequest;
import com.lms.library.borrow.dto.ReturnRequest;
import com.lms.library.borrow.dto.ReturnResponse;
import com.lms.library.borrow.entity.enums.MemberType;
import com.lms.library.borrow.service.IBorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrows")
@RequiredArgsConstructor
@Tag(name = "Borrow Management", description = "Operations related to book borrowing and returning")
public class BorrowController {

    private final IBorrowService borrowService;

    @Operation(summary = "Create a new borrowing record", 
               description = "Accepts a book borrowing request, validates policy, and initiates a Saga.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Borrowing process initiated (Saga started)"),
        @ApiResponse(responseCode = "400", description = "Invalid input or borrowing limit exceeded"),
        @ApiResponse(responseCode = "409", description = "Duplicate idempotency key detected")
    })
    @PostMapping
    public ResponseEntity<BorrowResponse> createBorrowing(
            @Parameter(description = "Member ID from Security Context (X-User-Id header)")
            @RequestHeader("X-User-Id") UUID memberId,
            
            @Parameter(description = "Member Type from Security Context (X-Member-Type header)")
            @RequestHeader("X-Member-Type") MemberType memberType,
            
            @Parameter(description = "Optional Idempotency Key to prevent double submission")
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            
            @Valid @RequestBody CreateBorrowRequest request) {

        BorrowResponse response = borrowService.createBorrowing(memberId, memberType, request, idempotencyKey);
        
        // Return 202 ACCEPTED considering Saga state is INITIATED
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @Operation(summary = "Process book return", 
               description = "Processes book return, updates status, calculates overdue fines, and creates return event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book return processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or borrow record not found"),
        @ApiResponse(responseCode = "403", description = "Borrow record does not belong to the member"),
        @ApiResponse(responseCode = "409", description = "Book has already been returned")
    })
    @PutMapping("/return")
    public ResponseEntity<ReturnResponse> processReturn(
            @Parameter(description = "Member ID from Security Context (X-User-Id header)")
            @RequestHeader("X-User-Id") UUID memberId,
            
            @Valid @RequestBody ReturnRequest request) {

        ReturnResponse response = borrowService.processReturn(memberId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Void> getBorrowById(@PathVariable UUID id) {
        // TODO: Implement GET detailed borrow record
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members/{mid}")
    public ResponseEntity<Void> getBorrowsByMember(@PathVariable UUID mid) {
        // TODO: Implement GET lists
        return ResponseEntity.ok().build();
    }
}
