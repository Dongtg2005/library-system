package com.lms.library.application.service;

import com.lms.library.application.dto.*;
import com.lms.library.domain.entity.*;
import com.lms.library.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookReviewService {

    private final BookReviewRepository reviewRepository;
    private final ReviewVoteRepository voteRepository;
    private final ReviewCommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getBookReviews(UUID bookId, Pageable pageable, Long currentUserId) {
        // Sort by helpfulCount desc, then createdAt desc for popularity
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "helpfulCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return reviewRepository.findByBookIdAndStatus(bookId, BookReview.ReviewStatus.PUBLISHED, sortedPageable)
                .map(review -> enrichReviewResponse(review, currentUserId));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getBookReviewsByPopularity(UUID bookId, Long currentUserId) {
        List<BookReview> reviews = reviewRepository.findByBookIdOrderByPopularity(bookId);
        return reviews.stream()
                .map(review -> enrichReviewResponse(review, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookRatingSummaryResponse getBookRatingSummary(UUID bookId) {
        Double avgRating = reviewRepository.calculateAverageRating(bookId);
        Long totalReviews = reviewRepository.countByBookId(bookId);

        // Calculate rating distribution
        List<BookReview> reviews = reviewRepository.findByBookId(bookId);
        Map<Integer, Long> distribution = reviews.stream()
                .filter(r -> r.getStatus() == BookReview.ReviewStatus.PUBLISHED)
                .collect(Collectors.groupingBy(BookReview::getRating, Collectors.counting()));

        // Ensure all ratings 1-5 are present
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        return BookRatingSummaryResponse.builder()
                .bookId(bookId)
                .averageRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional
    public ReviewResponse addReview(UUID bookId, Long userId, CreateReviewRequest request) {
        log.info("Adding review for book {} by user {}", bookId, userId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already reviewed this book - one review per user per book
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new RuntimeException("You have already reviewed this book. You can only review each book once.");
        }

        // Check if user has borrowed this book (must have ACTIVE or RETURNED status)
        boolean hasBorrowed = borrowRecordRepository.existsByMemberIdAndBookIdAndBorrowStatusIn(
                userId, bookId, List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.RETURNED));
        if (!hasBorrowed) {
            throw new RuntimeException("You can only review books you have borrowed.");
        }

        BookReview review = BookReview.builder()
                .bookId(bookId)
                .userId(userId)
                .reviewerName(user.getFullName()) // Display name instead of email
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

        return enrichReviewResponse(savedReview, userId);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, CreateReviewRequest request) {
        log.info("Updating review {} by user {}", reviewId, userId);

        BookReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Only review owner can update
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("You can only update your own reviews");
        }

        // Update fields
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());

        BookReview savedReview = reviewRepository.save(review);
        return enrichReviewResponse(savedReview, userId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review {} by user {}", reviewId, userId);

        BookReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Only review owner or admin can delete
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!review.getUserId().equals(userId) && !user.isAdmin()) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        review.setStatus(BookReview.ReviewStatus.DELETED);
        reviewRepository.save(review);
    }

    @Transactional
    public ReviewResponse voteReview(Long reviewId, Long userId, VoteRequest.VoteType voteType) {
        log.info("User {} voting {} on review {}", userId, voteType, reviewId);

        BookReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Check if user already voted
        Optional<ReviewVote> existingVote = voteRepository.findByReviewIdAndUserId(reviewId, userId);

        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();
            if (vote.getVoteType().name().equals(voteType.name())) {
                // Same vote - remove it (toggle off)
                voteRepository.delete(vote);
                // Update helpful count
                if (vote.isLike()) {
                    reviewRepository.decrementHelpfulCount(reviewId);
                }
            } else {
                // Different vote - update it
                ReviewVote.VoteType oldType = vote.getVoteType();
                vote.setVoteType(ReviewVote.VoteType.valueOf(voteType.name()));
                voteRepository.save(vote);

                // Update helpful count
                if (voteType == VoteRequest.VoteType.LIKE && oldType == ReviewVote.VoteType.DISLIKE) {
                    reviewRepository.incrementHelpfulCount(reviewId);
                } else if (voteType == VoteRequest.VoteType.DISLIKE && oldType == ReviewVote.VoteType.LIKE) {
                    reviewRepository.decrementHelpfulCount(reviewId);
                }
            }
        } else {
            // New vote
            ReviewVote newVote = ReviewVote.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .voteType(ReviewVote.VoteType.valueOf(voteType.name()))
                    .build();
            voteRepository.save(newVote);

            // Update helpful count for like
            if (voteType == VoteRequest.VoteType.LIKE) {
                reviewRepository.incrementHelpfulCount(reviewId);
            }
        }

        // Return updated review
        BookReview updatedReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return enrichReviewResponse(updatedReview, userId);
    }

    @Transactional(readOnly = true)
    public List<ReviewCommentResponse> getReviewComments(Long reviewId) {
        List<ReviewComment> comments = commentRepository.findByReviewIdAndStatusOrderByCreatedAtDesc(
                reviewId, ReviewComment.CommentStatus.PUBLISHED);

        // Group by parent for nested structure
        Map<Long, List<ReviewComment>> repliesMap = comments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ReviewComment::getParentId));

        // Build response with nested replies
        return comments.stream()
                .filter(c -> c.getParentId() == null) // Only top-level comments
                .map(comment -> {
                    ReviewCommentResponse response = ReviewCommentResponse.from(comment);
                    List<ReviewCommentResponse> replies = repliesMap.getOrDefault(comment.getId(), Collections.emptyList())
                            .stream()
                            .map(ReviewCommentResponse::from)
                            .collect(Collectors.toList());
                    response.setReplies(replies);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewCommentResponse addComment(Long userId, CreateCommentRequest request) {
        log.info("User {} adding comment to review {}", userId, request.getReviewId());

        // Verify review exists
        BookReview review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If parent comment specified, verify it exists and belongs to same review
        if (request.getParentId() != null) {
            ReviewComment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            if (!parentComment.getReviewId().equals(request.getReviewId())) {
                throw new RuntimeException("Parent comment does not belong to this review");
            }
        }

        ReviewComment comment = ReviewComment.builder()
                .reviewId(request.getReviewId())
                .userId(userId)
                .commenterName(user.getFullName()) // Display name instead of email
                .parentId(request.getParentId())
                .content(request.getContent())
                .status(ReviewComment.CommentStatus.PUBLISHED)
                .likeCount(0)
                .build();

        ReviewComment savedComment = commentRepository.save(comment);
        return ReviewCommentResponse.from(savedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("User {} deleting comment {}", userId, commentId);

        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only comment owner or admin can delete
        if (!comment.getUserId().equals(userId) && !user.isAdmin()) {
            throw new RuntimeException("You can only delete your own comments");
        }

        comment.setStatus(ReviewComment.CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Transactional
    public ReviewCommentResponse likeComment(Long commentId, Long userId) {
        log.info("User {} liking comment {}", userId, commentId);

        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // For simplicity, just increment like count
        // In a full implementation, you'd track which users liked which comments
        comment.setLikeCount(comment.getLikeCount() + 1);
        ReviewComment savedComment = commentRepository.save(comment);

        return ReviewCommentResponse.from(savedComment);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId, Long currentUserId) {
        BookReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return enrichReviewResponse(review, currentUserId);
    }

    private ReviewResponse enrichReviewResponse(BookReview review, Long currentUserId) {
        ReviewResponse response = ReviewResponse.from(review);

        // Get vote counts
        Long likeCount = voteRepository.countLikesByReviewId(review.getId());
        Long dislikeCount = voteRepository.countDislikesByReviewId(review.getId());
        response.setLikeCount(likeCount != null ? likeCount : 0);
        response.setDislikeCount(dislikeCount != null ? dislikeCount : 0);

        // Get comment count
        Long commentCount = commentRepository.countByReviewId(review.getId());
        response.setCommentCount(commentCount != null ? commentCount : 0);

        // Check if current user voted
        if (currentUserId != null) {
            voteRepository.findByReviewIdAndUserId(review.getId(), currentUserId)
                    .ifPresent(vote -> response.setUserVoteType(vote.getVoteType().name()));
        }

        // Get comments for this review (limited to top 3 for list view)
        List<ReviewCommentResponse> comments = getReviewComments(review.getId());
        response.setComments(comments);

        return response;
    }
}
