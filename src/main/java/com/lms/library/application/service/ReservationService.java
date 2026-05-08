package com.lms.library.application.service;

import com.lms.library.application.dto.ReservationResponse;
import com.lms.library.domain.entity.Book;
import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Reservation;
import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.exception.BookNotAvailableException;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.BorrowPolicyRepository;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.ReservationRepository;
import com.lms.library.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserProfileRepository userProfileRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowPolicyRepository borrowPolicyRepository;
    private final NotificationService notificationService;

    private static final int RESERVATION_EXPIRY_DAYS = 7;
    private static final int HOLD_EXPIRY_HOURS = 48; // 48 hours to pick up the book

    @Transactional
    public ReservationResponse createReservation(Long userId, UUID bookId, Integer priority, String notes) {
        log.info("Creating reservation for user: {}, book: {}", userId, bookId);

        // Validate user
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        // Validate book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book is available - only allow reservation if out of stock
        if (book.getAvailableQty() > 0) {
            throw new BookNotAvailableException("Book is currently available. Please borrow directly instead of reserving.");
        }

        // Check if user already has an active reservation for this book
        reservationRepository.findByUserIdAndBookIdAndStatus(userId, bookId, Reservation.ReservationStatus.ACTIVE)
                .ifPresent(r -> {
                    throw new IllegalStateException("You already have an active reservation for this book.");
                });

        // Calculate expiry date
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(RESERVATION_EXPIRY_DAYS);

        // Create reservation
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .bookId(bookId)
                .reservedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .status(Reservation.ReservationStatus.ACTIVE)
                .priority(priority != null ? priority : 1)
                .notes(notes)
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully: {}", reservation.getId());

        return ReservationResponse.from(reservation, book.getTitle(), userProfile.getFullName());
    }

    @Transactional
    public void cancelReservation(Long userId, UUID reservationId) {
        log.info("Cancelling reservation: {} for user: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Check if user owns this reservation
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own reservations");
        }

        // Check if reservation is still active
        if (!reservation.isActive()) {
            throw new IllegalStateException("Cannot cancel a reservation that is not active");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        log.info("Reservation cancelled successfully: {}", reservationId);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        log.info("Fetching reservations for user: {}", userId);
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(r -> {
                    Book book = bookRepository.findById(r.getBookId()).orElse(null);
                    UserProfile user = userProfileRepository.findByUserId(r.getUserId()).orElse(null);
                    return ReservationResponse.from(r, book != null ? book.getTitle() : null, user != null ? user.getFullName() : null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getBookReservations(UUID bookId) {
        log.info("Fetching reservations for book: {}", bookId);
        List<Reservation> reservations = reservationRepository.findActiveReservationsByBookIdOrdered(bookId);
        return reservations.stream()
                .map(r -> {
                    Book book = bookRepository.findById(r.getBookId()).orElse(null);
                    UserProfile user = userProfileRepository.findByUserId(r.getUserId()).orElse(null);
                    return ReservationResponse.from(r, book != null ? book.getTitle() : null, user != null ? user.getFullName() : null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAllReservations(Reservation.ReservationStatus status, Pageable pageable) {
        log.info("Fetching all reservations with status: {}", status);
        Page<Reservation> reservations;
        if (status != null) {
            reservations = reservationRepository.findByStatus(status, pageable);
        } else {
            reservations = reservationRepository.findAll(pageable);
        }
        return reservations.map(r -> {
            Book book = bookRepository.findById(r.getBookId()).orElse(null);
            UserProfile user = userProfileRepository.findByUserId(r.getUserId()).orElse(null);
            return ReservationResponse.from(r, book != null ? book.getTitle() : null, user != null ? user.getFullName() : null);
        });
    }

    @Transactional
    public void fulfillReservation(UUID reservationId) {
        log.info("Fulfilling reservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.canBeFulfilled()) {
            throw new IllegalStateException("Reservation cannot be fulfilled");
        }

        reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        log.info("Reservation fulfilled successfully: {}", reservationId);
    }

    /**
     * Auto-create borrow request for first reservation when book is returned
     * Creates a PENDING_APPROVAL borrow record for the user who reserved first
     * Runs in caller's transaction - do not add @Transactional to avoid rollback-only marking
     */
    public void autoFulfillFirstReservation(UUID bookId) {
        log.info("Auto-creating borrow request for first reservation of book: {}", bookId);

        // Find first active reservation ordered by priority and reserved time
        List<Reservation> reservations = reservationRepository.findActiveReservationsByBookIdOrdered(bookId);

        if (reservations.isEmpty()) {
            log.info("No active reservations found for book: {}", bookId);
            return;
        }

        Reservation firstReservation = reservations.get(0);

        // Check if reservation can be fulfilled (not expired)
        if (!firstReservation.canBeFulfilled()) {
            log.info("First reservation {} cannot be fulfilled", firstReservation.getId());
            return;
        }

        // Get book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!book.isAvailable()) {
            log.info("Book {} not available to fulfill reservation", bookId);
            return;
        }

        // Get user profile and borrow policy
        UserProfile userProfile = userProfileRepository.findByUserId(firstReservation.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        BorrowPolicy policy = borrowPolicyRepository.findByMemberType(BorrowPolicy.MemberType.USER)
                .orElseThrow(() -> new ResourceNotFoundException("No borrow policy found for USER"));

        // Check if policy is effective
        if (!policy.isEffective()) {
            log.warn("Borrow policy for USER is not effective, cannot auto-create borrow request");
            return;
        }

        // Reserve the book (decrease availableQty)
        book.borrowBook();
        bookRepository.save(book);
        log.info("Book {} reserved for user {}", bookId, firstReservation.getUserId());

        // Create borrow record with PENDING_APPROVAL status
        LocalDate now = LocalDate.now();
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .memberId(firstReservation.getUserId())
                .bookId(bookId)
                .borrowDate(now)
                .dueDate(now.plusDays(policy.getLoanPeriodDays()))
                .borrowStatus(BorrowRecord.BorrowStatus.PENDING_APPROVAL)
                .conditionOnBorrow(BorrowRecord.BookCondition.GOOD)
                .reservationId(firstReservation.getId())
                .notes("Auto-created from reservation")
                .build();

        borrowRecordRepository.save(borrowRecord);
        log.info("Created borrow request {} for reservation {}", borrowRecord.getId(), firstReservation.getId());

        // Mark reservation as fulfilled
        firstReservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        firstReservation.setFulfilledAt(LocalDateTime.now());
        reservationRepository.save(firstReservation);

        // Increment user's borrowed count
        userProfile.incrementBorrowedCount();
        userProfileRepository.save(userProfile);

        // Notify user that book is available and borrow request is created
        try {
            notificationService.notifyUserBookOnHold(
                firstReservation.getUserId(),
                book.getTitle(),
                bookId,
                HOLD_EXPIRY_HOURS
            );
        } catch (Exception e) {
            log.error("Failed to create notification for user", e);
        }

        log.info("Reservation {} fulfilled - borrow request {} created for user {}",
                firstReservation.getId(), borrowRecord.getId(), firstReservation.getUserId());
    }

    /**
     * User confirms borrowing from ON_HOLD status
     */
    @Transactional
    public ReservationResponse confirmBorrowFromHold(Long userId, UUID reservationId) {
        log.info("User {} confirming borrow from hold: {}", userId, reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Check ownership
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only confirm your own reservations");
        }

        // Check status and expiry
        if (!reservation.isOnHold()) {
            throw new IllegalStateException("Reservation is not on hold");
        }

        if (reservation.isHoldExpired()) {
            throw new IllegalStateException("Hold has expired. The book has been released to the next person.");
        }

        // Create borrow record
        Book book = bookRepository.findById(reservation.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        BorrowPolicy policy = borrowPolicyRepository.findAllByMemberTypeOrderByCreatedAtDesc(BorrowPolicy.MemberType.USER)
                .stream()
                .findFirst()
                .orElse(null);

        int loanPeriodDays = (policy != null) ? policy.getLoanPeriodDays() : 14;
        int maxExtensions = (policy != null) ? policy.getMaxExtensions() : 2;

        LocalDate today = LocalDate.now();
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .memberId(userId)
                .bookId(reservation.getBookId())
                .reservationId(reservation.getId())
                .borrowDate(today)
                .borrowTime(ZonedDateTime.now())
                .dueDate(today.plusDays(loanPeriodDays))
                .maxExtensions(maxExtensions)
                .borrowStatus(BorrowRecord.BorrowStatus.ACTIVE)
                .notes("Confirmed from reservation hold")
                .build();

        borrowRecordRepository.save(borrowRecord);
        log.info("Created borrow record {} for user {}", borrowRecord.getId(), userId);

        // Update user profile
        userProfile.incrementBorrowedCount();
        userProfileRepository.save(userProfile);

        // Mark reservation as fulfilled
        reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation, book.getTitle(), userProfile.getFullName());
    }

    /**
     * Auto-expire ON_HOLD reservations after 48h and notify next person
     */
    @Transactional
    public void expireExpiredHolds() {
        log.info("Checking for expired holds");
        List<Reservation> expiredHolds = reservationRepository.findExpiredHolds(LocalDateTime.now());
        
        for (Reservation reservation : expiredHolds) {
            log.info("Expiring hold for reservation: {}", reservation.getId());
            
            // Get book and release it
            Book book = bookRepository.findById(reservation.getBookId()).orElse(null);
            if (book != null) {
                // Return the book (increases availableQty)
                book.returnBook();
                bookRepository.save(book);
                
                // Try to fulfill next reservation
                autoFulfillFirstReservation(reservation.getBookId());
            }
            
            // Notify user their hold expired
            try {
                notificationService.notifyUserHoldExpired(
                    reservation.getUserId(),
                    book != null ? book.getTitle() : "Book",
                    reservation.getBookId()
                );
            } catch (Exception e) {
                log.error("Failed to create hold expired notification", e);
            }
        }
        
        log.info("Expired {} holds", expiredHolds.size());
    }

    @Transactional
    public void markExpiredReservations() {
        log.info("Marking expired reservations");
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());
        expiredReservations.forEach(r -> {
            r.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(r);
        });
        log.info("Marked {} reservations as expired", expiredReservations.size());
    }

    @Transactional(readOnly = true)
    public long getReservationCount(UUID bookId) {
        return reservationRepository.countByBookIdAndStatus(bookId, Reservation.ReservationStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ReservationResponse checkReservationStatus(Long userId, UUID bookId) {
        log.info("Checking reservation status for user: {} and book: {}", userId, bookId);

        // Check for ON_HOLD first (most relevant for user action)
        Reservation onHoldReservation = reservationRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, Reservation.ReservationStatus.ON_HOLD)
                .orElse(null);

        if (onHoldReservation != null) {
            Book book = bookRepository.findById(bookId).orElse(null);
            UserProfile user = userProfileRepository.findByUserId(userId).orElse(null);
            return ReservationResponse.from(onHoldReservation,
                    book != null ? book.getTitle() : null,
                    user != null ? user.getFullName() : null);
        }

        // Check for ACTIVE reservation
        Reservation activeReservation = reservationRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, Reservation.ReservationStatus.ACTIVE)
                .orElse(null);

        if (activeReservation != null) {
            Book book = bookRepository.findById(bookId).orElse(null);
            UserProfile user = userProfileRepository.findByUserId(userId).orElse(null);
            return ReservationResponse.from(activeReservation,
                    book != null ? book.getTitle() : null,
                    user != null ? user.getFullName() : null);
        }

        return null;
    }
}
