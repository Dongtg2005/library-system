package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review_comments", indexes = {
    @Index(name = "idx_comment_review_id", columnList = "review_id"),
    @Index(name = "idx_comment_user_id", columnList = "user_id"),
    @Index(name = "idx_comment_parent_id", columnList = "parent_id"),
    @Index(name = "idx_comment_created_at", columnList = "created_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "commenter_name", nullable = false, length = 255)
    private String commenterName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CommentStatus status = CommentStatus.PUBLISHED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (status == null) status = CommentStatus.PUBLISHED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CommentStatus {
        PUBLISHED, HIDDEN, DELETED
    }
}
