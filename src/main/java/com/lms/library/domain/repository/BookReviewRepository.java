package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Page<BookReview> findByBookIdAndStatus(UUID bookId, BookReview.ReviewStatus status, Pageable pageable);

    Optional<BookReview> findByBookIdAndUserId(UUID bookId, Long userId);

    List<BookReview> findByBookId(UUID bookId);

    boolean existsByBookIdAndUserId(UUID bookId, Long userId);

    @Query("SELECT AVG(br.rating) FROM BookReview br WHERE br.bookId = :bookId AND br.status = 'PUBLISHED'")
    Double calculateAverageRating(@Param("bookId") UUID bookId);

    @Query("SELECT COUNT(br) FROM BookReview br WHERE br.bookId = :bookId AND br.status = 'PUBLISHED'")
    Long countByBookId(@Param("bookId") UUID bookId);

    @Query("SELECT br FROM BookReview br WHERE br.bookId = :bookId AND br.status = 'PUBLISHED' ORDER BY br.helpfulCount DESC, br.createdAt DESC")
    List<BookReview> findByBookIdOrderByPopularity(@Param("bookId") UUID bookId);

    Page<BookReview> findByBookIdAndStatusOrderByHelpfulCountDescCreatedAtDesc(UUID bookId, BookReview.ReviewStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE BookReview br SET br.helpfulCount = br.helpfulCount + 1 WHERE br.id = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE BookReview br SET br.helpfulCount = CASE WHEN br.helpfulCount > 0 THEN br.helpfulCount - 1 ELSE 0 END WHERE br.id = :reviewId")
    void decrementHelpfulCount(@Param("reviewId") Long reviewId);
}
