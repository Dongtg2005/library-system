package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.application.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final BookReviewService reviewService;
    private final AuthenticationService authenticationService;

    // ==================== Book Reviews ====================

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable UUID bookId,
            Pageable pageable) {
        Long currentUserId = authenticationService.getCurrentUserIdOrNull();
        return ResponseEntity.ok(reviewService.getBookReviews(bookId, pageable, currentUserId));
    }

    @GetMapping("/books/{bookId}/reviews/popular")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPopularity(
            @PathVariable UUID bookId) {
        Long currentUserId = authenticationService.getCurrentUserIdOrNull();
        return ResponseEntity.ok(reviewService.getBookReviewsByPopularity(bookId, currentUserId));
    }

    @GetMapping("/books/{bookId}/reviews/summary")
    public ResponseEntity<BookRatingSummaryResponse> getBookRatingSummary(
            @PathVariable UUID bookId) {
        return ResponseEntity.ok(reviewService.getBookRatingSummary(bookId));
    }

    @PostMapping("/books/{bookId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateReviewRequest request) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.addReview(bookId, userId, request));
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long userId = authenticationService.getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        Long currentUserId = authenticationService.getCurrentUserIdOrNull();
        return ResponseEntity.ok(reviewService.getReviewById(reviewId, currentUserId));
    }

    // ==================== Review Votes ====================

    @PostMapping("/reviews/vote")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> voteReview(@Valid @RequestBody VoteRequest request) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.voteReview(request.getReviewId(), userId, request.getVoteType()));
    }

    // ==================== Review Comments ====================

    @GetMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<List<ReviewCommentResponse>> getReviewComments(
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewComments(reviewId));
    }

    @PostMapping("/reviews/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewCommentResponse> addComment(
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.addComment(userId, request));
    }

    @DeleteMapping("/reviews/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = authenticationService.getCurrentUserId();
        reviewService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/comments/{commentId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewCommentResponse> likeComment(@PathVariable Long commentId) {
        Long userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(reviewService.likeComment(commentId, userId));
    }
}
