package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "book_reviews", indexes = {
    @Index(name = "idx_review_book_id", columnList = "book_id"),
    @Index(name = "idx_review_user_id", columnList = "user_id"),
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_review_book_user", columnNames = {"book_id", "user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    /** Full name snapshot at time of review (in case user changes later) */
    @Column(name = "reviewer_name", nullable = false, length = 255)
    private String reviewerName;

    @Column(nullable = false)
    private Integer rating;   // 1-5

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
        if (helpfulCount == null) helpfulCount = 0;
        if (status == null) status = ReviewStatus.PUBLISHED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReviewStatus {
        PUBLISHED, HIDDEN, DELETED
    }

    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }
}
