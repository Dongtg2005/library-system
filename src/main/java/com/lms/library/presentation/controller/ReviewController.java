package com.lms.library.presentation.controller;

import com.lms.library.application.dto.CreateReviewRequest;
import com.lms.library.application.dto.ReviewResponse;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books/{bookId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final BookReviewService reviewService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable UUID bookId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getBookReviews(bookId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateReviewRequest request) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.addReview(bookId, userId, request));
    }
}
