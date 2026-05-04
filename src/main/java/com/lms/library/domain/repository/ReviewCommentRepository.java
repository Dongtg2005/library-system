package com.lms.library.domain.repository;

import com.lms.library.domain.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    List<ReviewComment> findByReviewIdAndStatusOrderByCreatedAtDesc(Long reviewId, ReviewComment.CommentStatus status);

    Page<ReviewComment> findByReviewIdAndStatus(Long reviewId, ReviewComment.CommentStatus status, Pageable pageable);

    List<ReviewComment> findByParentIdAndStatusOrderByCreatedAtAsc(Long parentId, ReviewComment.CommentStatus status);

    @Query("SELECT rc FROM ReviewComment rc WHERE rc.reviewId = :reviewId AND rc.status = 'PUBLISHED' ORDER BY rc.likeCount DESC, rc.createdAt DESC")
    List<ReviewComment> findByReviewIdOrderByPopularity(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.reviewId = :reviewId AND rc.status = 'PUBLISHED'")
    Long countByReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE ReviewComment rc SET rc.likeCount = rc.likeCount + 1 WHERE rc.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE ReviewComment rc SET rc.likeCount = CASE WHEN rc.likeCount > 0 THEN rc.likeCount - 1 ELSE 0 END WHERE rc.id = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);
}
