package com.lms.library.domain.repository;

import com.lms.library.domain.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    Optional<ReviewVote> findByReviewIdAndUserId(Long reviewId, Long userId);

    List<ReviewVote> findByReviewId(Long reviewId);

    @Query("SELECT rv.voteType, COUNT(rv) FROM ReviewVote rv WHERE rv.reviewId = :reviewId GROUP BY rv.voteType")
    List<Object[]> countVotesByReviewId(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(rv) FROM ReviewVote rv WHERE rv.reviewId = :reviewId AND rv.voteType = 'LIKE'")
    Long countLikesByReviewId(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(rv) FROM ReviewVote rv WHERE rv.reviewId = :reviewId AND rv.voteType = 'DISLIKE'")
    Long countDislikesByReviewId(@Param("reviewId") Long reviewId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}
