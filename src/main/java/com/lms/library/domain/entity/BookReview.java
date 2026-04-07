package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReview {
    private Long id;
    private Long userId;
    private UUID bookId;
    private Integer rating;
    private String title;
    private String content;
    private Integer helpfulCount;
    private Boolean verifiedPurchase;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum ReviewStatus {
        PUBLISHED, HIDDEN, DELETED
    }
    
    public boolean isPublished() {
        return ReviewStatus.PUBLISHED.equals(status);
    }
    
    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }
    
    public void incrementHelpfulCount() {
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
        helpfulCount++;
    }
}
