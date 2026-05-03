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

    @Transactional
    public void autoFulfillFirstReservation(UUID bookId) {
        log.info("Auto-fulfilling first reservation for book: {}", bookId);

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

        // Get book and reserve it (decrease availableQty to hold for this user)
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        if (book.isAvailable()) {
            // Reserve the book by borrowing it (decreases availableQty)
            book.borrowBook();
            bookRepository.save(book);
            log.info("Book {} reserved for user {}", bookId, firstReservation.getUserId());
        } else {
            log.info("Book {} not available to reserve", bookId);
            return;
        }

        // Create pending borrow record for the user
        UserProfile userProfile = userProfileRepository.findByUserId(firstReservation.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        
        BorrowPolicy policy = borrowPolicyRepository.findAllByMemberTypeOrderByCreatedAtDesc(BorrowPolicy.MemberType.USER)
                .stream()
                .findFirst()
                .orElse(null);
        
        int loanPeriodDays = (policy != null) ? policy.getLoanPeriodDays() : 14;
        int maxExtensions = (policy != null) ? policy.getMaxExtensions() : 2;
        
        LocalDate now = LocalDate.now();
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .memberId(firstReservation.getUserId())
                .bookId(bookId)
                .reservationId(firstReservation.getId())
                .borrowDate(now)
                .borrowTime(ZonedDateTime.now())
                .dueDate(now.plusDays(loanPeriodDays))
                .maxExtensions(maxExtensions)
                .borrowStatus(BorrowRecord.BorrowStatus.PENDING_APPROVAL)
                .notes("Auto-created from reservation")
                .build();
        
        borrowRecordRepository.save(borrowRecord);
        log.info("Created pending borrow record {} for user {}", borrowRecord.getId(), firstReservation.getUserId());

        // Update user profile borrowed count
        userProfile.incrementBorrowedCount();
        userProfileRepository.save(userProfile);

        // Mark as fulfilled
        firstReservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        firstReservation.setFulfilledAt(LocalDateTime.now());
        reservationRepository.save(firstReservation);

        // Notify user that book is available
        try {
            notificationService.notifyUserBookAvailable(
                firstReservation.getUserId(),
                book.getTitle(),
                bookId
            );
        } catch (Exception e) {
            log.error("Failed to create notification for user", e);
        }

        log.info("Auto-fulfilled reservation {} for book {}", firstReservation.getId(), bookId);
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
}
