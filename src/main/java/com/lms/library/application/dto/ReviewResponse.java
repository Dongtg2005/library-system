package com.lms.library.application.dto;

import com.lms.library.domain.entity.BookReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String reviewerName;
    private Integer rating;
    private String title;
    private String content;
    private Integer helpfulCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private String userVoteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewCommentResponse> comments;

    public static ReviewResponse from(BookReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .reviewerName(review.getReviewerName())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
