package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Page<BookReview> findByBookIdAndStatus(UUID bookId, BookReview.ReviewStatus status, Pageable pageable);
    
    Optional<BookReview> findByBookIdAndUserId(UUID bookId, Long userId);
    
    List<BookReview> findByBookId(UUID bookId);
}
