package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review_votes", indexes = {
    @Index(name = "idx_vote_review_id", columnList = "review_id"),
    @Index(name = "idx_vote_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_vote_review_user", columnNames = {"review_id", "user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10)
    private VoteType voteType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum VoteType {
        LIKE, DISLIKE
    }

    public boolean isLike() {
        return VoteType.LIKE.equals(voteType);
    }

    public boolean isDislike() {
        return VoteType.DISLIKE.equals(voteType);
    }
}
