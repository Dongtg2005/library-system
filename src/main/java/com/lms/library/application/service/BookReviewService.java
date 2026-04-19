package com.lms.library.application.service;

import com.lms.library.application.dto.CreateReviewRequest;
import com.lms.library.application.dto.ReviewResponse;
import com.lms.library.domain.entity.Book;
import com.lms.library.domain.entity.BookReview;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.BookReviewRepository;
import com.lms.library.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookReviewService {

    private final BookReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getBookReviews(UUID bookId, Pageable pageable) {
        return reviewRepository.findByBookIdAndStatus(bookId, BookReview.ReviewStatus.PUBLISHED, pageable)
                .map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse addReview(UUID bookId, Long userId, CreateReviewRequest request) {
        log.info("Adding review for book {} by user {}", bookId, userId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already reviewed this book
        reviewRepository.findByBookIdAndUserId(bookId, userId).ifPresent(r -> {
            throw new RuntimeException("You have already reviewed this book");
        });

        BookReview review = BookReview.builder()
                .bookId(bookId)
                .userId(userId)
                .reviewerName(user.getFullName())
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .status(BookReview.ReviewStatus.PUBLISHED)
                .helpfulCount(0)
                .build();

        BookReview savedReview = reviewRepository.save(review);

        // Update book rating
        book.addRating(request.getRating());
        bookRepository.save(book);

        return ReviewResponse.from(savedReview);
    }
}
