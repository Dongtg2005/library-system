package com.lms.library.application.dto;

import com.lms.library.domain.entity.ReviewComment;
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
public class ReviewCommentResponse {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String commenterName;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewCommentResponse> replies;

    public static ReviewCommentResponse from(ReviewComment comment) {
        return ReviewCommentResponse.builder()
                .id(comment.getId())
                .reviewId(comment.getReviewId())
                .userId(comment.getUserId())
                .commenterName(comment.getCommenterName())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .status(comment.getStatus().name())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
