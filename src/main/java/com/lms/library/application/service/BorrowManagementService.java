package com.lms.library.application.service;

import com.lms.library.domain.entity.Book;
import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Fine;
import com.lms.library.domain.entity.Reservation;
import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.BorrowPolicyRepository;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.FineRepository;
import com.lms.library.domain.repository.UserProfileRepository;
import com.lms.library.application.dto.*;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.exception.PolicyNotFoundException;
import com.lms.library.domain.exception.BorrowLimitExceededException;
import com.lms.library.domain.exception.ForbiddenOperationException;
import com.lms.library.domain.exception.CardExpiredException;
import com.lms.library.domain.exception.OutstandingFineException;
import com.lms.library.domain.exception.BookNotAvailableException;
import com.lms.library.domain.exception.BookAlreadyBorrowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowManagementService {
    
    private final BorrowPolicyRepository borrowPolicyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final UserProfileRepository userProfileRepository;
    private final BookRepository bookRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getAllBorrows(BorrowRecord.BorrowStatus status, Pageable pageable) {
        log.info("Fetching all borrows with status: {}", status);
        Page<BorrowRecord> records;
        if (status != null) {
            records = borrowRecordRepository.findByBorrowStatus(status, pageable);
        } else {
            records = borrowRecordRepository.findAll(pageable);
        }
        return records.map(this::toBorrowResponse);
    }
    
    @Transactional
    public BorrowResponse createBorrowing(Long memberId, BorrowPolicy.MemberType memberType, CreateBorrowRequest request) {
        log.info("Processing borrow request for member: {}", memberId);

        // Fetch User Profile for validation
        UserProfile userProfile = userProfileRepository.findByUserId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for member: " + memberId));

        // Fetch Book for validation
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + request.getBookId()));

        // Fetch Borrow Policy
        BorrowPolicy policy = borrowPolicyRepository.findAllByMemberTypeOrderByCreatedAtDesc(memberType)
                .stream()
                .findFirst()
                .orElseThrow(() -> new PolicyNotFoundException("Borrow policy not found for member type: " + memberType));

        // ========================================
        // VALIDATION FLOW
        // ========================================

        // 1. Check card expiry
        if (userProfile.isCardExpired()) {
            throw new CardExpiredException("Your library card has expired. Please renew your membership.");
        }

        // 2. Check outstanding fines
        if (userProfile.hasOutstandingFines()) {
            throw new OutstandingFineException(
                String.format("You have outstanding fines of %s VND. Please pay your fines before borrowing.",
                    userProfile.getOutstandingFines()));
        }

        // 3. Check borrow limit within policy window (e.g., max 5 in 14 days)
        java.time.LocalDateTime currentDateTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime windowStart = currentDateTime.minusDays(policy.getLoanPeriodDays());

        int recentBorrows = borrowRecordRepository.countByMemberIdAndCreatedAtBetweenAndBorrowStatusIn(
            memberId,
            windowStart,
            currentDateTime,
            List.of(
                BorrowRecord.BorrowStatus.ACTIVE,
                BorrowRecord.BorrowStatus.PENDING_APPROVAL,
                BorrowRecord.BorrowStatus.RETURNED,
                BorrowRecord.BorrowStatus.OVERDUE
            )
        );

        if (recentBorrows >= policy.getMaxBooksAllowed()) {
            throw new BorrowLimitExceededException(
                String.format("Bạn đã mượn %d cuốn trong %d ngày qua. Giới hạn là %d cuốn.",
                    recentBorrows, policy.getLoanPeriodDays(), policy.getMaxBooksAllowed())
            );
        }

        // 4. Check book availability
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("This book is currently not available for borrowing.");
        }

        // 5. Check if user already borrowed this book
        var existingBorrow = borrowRecordRepository.findByMemberIdAndBookIdAndBorrowStatusIn(
                memberId, request.getBookId(),
                List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.PENDING_APPROVAL));
        if (existingBorrow.isPresent()) {
            throw new BookAlreadyBorrowedException("You have already borrowed or requested this book.");
        }

        // ========================================
        // CREATE BORROW RECORD
        // ========================================

        LocalDate now = LocalDate.now();

        // Create Borrow Record with PENDING_APPROVAL status
        BorrowRecord record = BorrowRecord.builder()
                .memberId(memberId)
                .bookId(request.getBookId())
                .borrowDate(now)
                .borrowTime(java.time.ZonedDateTime.now())
                .dueDate(now.plusDays(policy.getLoanPeriodDays()))
                .maxExtensions(policy.getMaxExtensions())
                .borrowStatus(BorrowRecord.BorrowStatus.PENDING_APPROVAL)
                .conditionOnBorrow(request.getConditionOnBorrow())
                .notes(request.getNotes())
                .build();

        record = borrowRecordRepository.save(record);

        // Update book availability
        book.borrowBook();
        bookRepository.save(book);

        // Update user profile borrowed count
        userProfile.incrementBorrowedCount();
        userProfileRepository.save(userProfile);

        // Calculate and create fine if needed (for overdue books)
        calculateOverdueFines(memberId);

        log.info("Borrow request created with ID: {}, Status: PENDING_APPROVAL", record.getId());

        // Notify librarians about new borrow request
        try {
            String memberName = userProfileRepository.findByUserId(memberId)
                    .map(UserProfile::getFullName)
                    .orElse("Member #" + memberId);
            notificationService.notifyLibrarianNewBorrowRequest(
                memberName,
                book.getTitle(),
                book.getId()
            );
        } catch (Exception e) {
            log.error("Failed to create notification for librarians", e);
        }

        return toBorrowResponse(record);
    }

    @Transactional
    public BorrowResponse approveBorrowRequest(UUID borrowRecordId, Long librarianId) {
        log.info("Approving borrow request: {} by librarian: {}", borrowRecordId, librarianId);

        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        if (record.getBorrowStatus() != BorrowRecord.BorrowStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Borrow request is not in pending approval status");
        }

        record.setBorrowStatus(BorrowRecord.BorrowStatus.ACTIVE);
        record.setLibrarianId(librarianId);
        record = borrowRecordRepository.save(record);

        // Notify user about approval
        try {
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            notificationService.notifyUserApproved(
                record.getMemberId(),
                book.getTitle(),
                record.getBookId()
            );
        } catch (Exception e) {
            log.error("Failed to create notification for user approval", e);
        }

        log.info("Borrow request {} approved", borrowRecordId);
        return toBorrowResponse(record);
    }

    @Transactional
    public BorrowResponse rejectBorrowRequest(UUID borrowRecordId, Long librarianId, String reason) {
        log.info("Rejecting borrow request: {} by librarian: {}, reason: {}", borrowRecordId, librarianId, reason);

        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        if (record.getBorrowStatus() != BorrowRecord.BorrowStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Borrow request is not in pending approval status");
        }

        // Return book to inventory
        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.returnBook();
        bookRepository.save(book);

        // Decrement user's borrowed count
        UserProfile userProfile = userProfileRepository.findByUserId(record.getMemberId())
                .orElse(null);
        if (userProfile != null) {
            userProfile.decrementBorrowedCount();
            userProfileRepository.save(userProfile);
        }

        record.setBorrowStatus(BorrowRecord.BorrowStatus.CANCELLED);
        record.setLibrarianId(librarianId);
        record.setRejectionReason(reason);
        record = borrowRecordRepository.save(record);

        // Notify user about rejection
        try {
            notificationService.notifyUserRejected(
                record.getMemberId(),
                book.getTitle(),
                reason != null ? reason : "Không có lý do"
            );
        } catch (Exception e) {
            log.error("Failed to create notification for user rejection", e);
        }

        log.info("Borrow request {} rejected with reason: {}", borrowRecordId, reason);
        return toBorrowResponse(record);
    }
    
    @Transactional
    public ReturnResponse processReturn(Long memberId, ReturnRequest request) {
        log.info("Processing return for member: {}", memberId);
        
        BorrowRecord record = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
        
        if (!record.getMemberId().equals(memberId)) {
            throw new ForbiddenOperationException("Borrow record does not belong to this member");
        }
        
        // Calculate overdue fine if applicable
        BigDecimal overdueFine = calculateOverdueFine(record);

        // Fetch user profile
        UserProfile userProfile = userProfileRepository.findByUserId(memberId)
                .orElse(null);

        // Process fine update before changing status to RETURNED
        updateOverdueFineForRecord(record, userProfile);

        // Return book to inventory
        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.returnBook();
        bookRepository.save(book);

        // Auto-fulfill first reservation if available
        if (book.isAvailable()) {
            try {
                reservationService.autoFulfillFirstReservation(book.getId());
            } catch (Exception e) {
                log.error("Failed to auto-fulfill reservation for book: {}", book.getId(), e);
                // Continue with return process even if auto-fulfill fails
            }
        }

        // Decrement user's borrowed count
        if (userProfile != null) {
            userProfile.decrementBorrowedCount();
            userProfileRepository.save(userProfile);
        }

        // Mark book as returned
        record.returnBook(request.getConditionOnReturn());
        borrowRecordRepository.save(record);

        // Notify librarian about book return
        try {
            String memberName = userProfile != null ? userProfile.getFullName() : "Member #" + memberId;
            notificationService.notifyLibrarianBookReturned(
                memberName,
                book.getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to create notification for librarian", e);
        }

        // Notify user about fine creation
        if (overdueFine.compareTo(BigDecimal.ZERO) > 0) {
            try {
                notificationService.notifyUserFineCreated(memberId, overdueFine, book.getTitle());
            } catch (Exception e) {
                log.error("Failed to notify user about fine creation", e);
            }
        }

        return ReturnResponse.builder()
                .borrowRecordId(record.getId())
                .returnDate(record.getReturnDate())
                .conditionOnReturn(record.getConditionOnReturn())
                .overdueFine(overdueFine)
                .build();
    }

    @Transactional
    public ReturnResponse processReturnByLibrarian(ReturnRequest request) {
        log.info("Processing librarian return for borrow record: {}", request.getBorrowRecordId());

        BorrowRecord record = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        // Calculate overdue fine if applicable
        BigDecimal overdueFine = calculateOverdueFine(record);

        // Fetch user profile
        UserProfile userProfile = userProfileRepository.findByUserId(record.getMemberId())
                .orElse(null);

        // Process fine update before changing status to RETURNED
        updateOverdueFineForRecord(record, userProfile);

        // Return book to inventory
        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.returnBook();
        bookRepository.save(book);

        // Auto-fulfill first reservation if available
        if (book.isAvailable()) {
            try {
                reservationService.autoFulfillFirstReservation(book.getId());
            } catch (Exception e) {
                log.error("Failed to auto-fulfill reservation for book: {}", book.getId(), e);
            }
        }

        // Decrement user's borrowed count (use record's memberId, not current user)
        if (userProfile != null) {
            userProfile.decrementBorrowedCount();
            userProfileRepository.save(userProfile);
        }

        // Mark book as returned
        record.returnBook(request.getConditionOnReturn());
        borrowRecordRepository.save(record);

        // Notify librarian about book return
        try {
            String memberName = userProfile != null ? userProfile.getFullName() : "Member #" + record.getMemberId();
            notificationService.notifyLibrarianBookReturned(
                memberName,
                book.getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to create notification for librarian", e);
        }

        // Notify user about fine creation
        if (overdueFine.compareTo(BigDecimal.ZERO) > 0) {
            try {
                notificationService.notifyUserFineCreated(record.getMemberId(), overdueFine, book.getTitle());
            } catch (Exception e) {
                log.error("Failed to notify user about fine creation", e);
            }
        }

        return ReturnResponse.builder()
                .borrowRecordId(record.getId())
                .returnDate(record.getReturnDate())
                .conditionOnReturn(record.getConditionOnReturn())
                .overdueFine(overdueFine)
                .build();
    }

    @Transactional
    public BorrowResponse extendLoan(UUID borrowRecordId, Long memberId) {
        log.info("Extending loan for record: {}", borrowRecordId);
        
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
        
        if (!record.getMemberId().equals(memberId)) {
            throw new ForbiddenOperationException("Borrow record does not belong to this member");
        }
        
        if (!record.canExtend()) {
            throw new IllegalStateException("Cannot extend loan for this record");
        }
        
        record.extendLoan(7); // Extend by 7 days
        borrowRecordRepository.save(record);
        
        return toBorrowResponse(record);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowResponse> getMemberBorrowHistory(Long memberId) {
        List<BorrowRecord> records = borrowRecordRepository.findByMemberId(memberId);
        return records.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BorrowResponse checkBorrowStatus(Long memberId, UUID bookId) {
        var existingBorrow = borrowRecordRepository.findByMemberIdAndBookIdAndBorrowStatusIn(
                memberId, bookId,
                List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.PENDING_APPROVAL));
        return existingBorrow.map(this::toBorrowResponse).orElse(null);
    }

    private BorrowResponse toBorrowResponse(BorrowRecord record) {
        String memberName = userProfileRepository.findByUserId(record.getMemberId())
                .map(UserProfile::getFullName)
                .orElse("Member #" + record.getMemberId());
        
        Book book = bookRepository.findById(record.getBookId()).orElse(null);
        String bookTitle = book != null ? book.getTitle() : "Unknown Book";
        String bookAuthor = book != null ? book.getAuthor() : "Unknown Author";
        String coverImageUrl = book != null ? book.getCoverImageUrl() : null;

        BorrowResponse response = BorrowResponse.from(record, memberName);
        response.setBookTitle(bookTitle);
        response.setBookAuthor(bookAuthor);
        response.setCoverImageUrl(coverImageUrl);
        response.setFineAmount(record.getFineAmount());
        response.setFinePaid(record.getFinePaid());
        response.setFinePaidAt(record.getFinePaidAt());
        response.setOverdueDays(record.getOverdueDays());
        
        return response;
    }
    
    private void calculateOverdueFines(Long memberId) {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByMemberId(memberId)
                .stream()
                .filter(BorrowRecord::isOverdue)
                .toList();
        
        UserProfile userProfile = userProfileRepository.findByUserId(memberId).orElse(null);
        for (BorrowRecord record : overdueRecords) {
            updateOverdueFineForRecord(record, userProfile);
        }
    }

    private BigDecimal updateOverdueFineForRecord(BorrowRecord record, UserProfile userProfile) {
        if (!record.isOverdue()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalOverdueFine = calculateOverdueFine(record);
        if (totalOverdueFine.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Find total paid overdue fines for this record
        BigDecimal totalPaidFines = fineRepository.findByBorrowRecordId(record.getId()).stream()
                .filter(f -> f.getFineType() == Fine.FineType.OVERDUE && f.getStatus() == Fine.FineStatus.PAID)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newPendingAmount = totalOverdueFine.subtract(totalPaidFines);
        if (newPendingAmount.compareTo(BigDecimal.ZERO) < 0) {
            newPendingAmount = BigDecimal.ZERO;
        }

        // Find existing pending overdue fine
        Fine pendingFine = fineRepository.findByBorrowRecordId(record.getId()).stream()
                .filter(f -> f.getFineType() == Fine.FineType.OVERDUE && f.getStatus() == Fine.FineStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (pendingFine != null) {
            BigDecimal difference = newPendingAmount.subtract(pendingFine.getAmount());
            pendingFine.setAmount(newPendingAmount);
            pendingFine.setReason("Book overdue by " + record.getOverdueDays() + " days");
            fineRepository.save(pendingFine);

            if (userProfile != null) {
                BigDecimal newOutstanding = userProfile.getOutstandingFines().add(difference);
                if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                    newOutstanding = BigDecimal.ZERO;
                }
                userProfile.setOutstandingFines(newOutstanding);
                userProfileRepository.save(userProfile);
            }
        } else if (newPendingAmount.compareTo(BigDecimal.ZERO) > 0) {
            Fine newFine = Fine.builder()
                    .borrowRecordId(record.getId())
                    .memberId(record.getMemberId())
                    .amount(newPendingAmount)
                    .fineType(Fine.FineType.OVERDUE)
                    .status(Fine.FineStatus.PENDING)
                    .reason("Book overdue by " + record.getOverdueDays() + " days")
                    .build();
            fineRepository.save(newFine);

            if (userProfile != null) {
                userProfile.setOutstandingFines(userProfile.getOutstandingFines().add(newPendingAmount));
                userProfileRepository.save(userProfile);
            }
        }

        // Update BorrowRecord's fine details
        record.setFineAmount(totalOverdueFine);
        record.setFinePaid(newPendingAmount.compareTo(BigDecimal.ZERO) == 0);
        borrowRecordRepository.save(record);

        return newPendingAmount;
    }

    @Transactional
    public void runDailyOverdueAndFineCalculation() {
        log.info("Starting daily overdue and fine calculation job");
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords();
        log.info("Found {} overdue records to process", overdueRecords.size());

        for (BorrowRecord record : overdueRecords) {
            try {
                // If status was ACTIVE, mark as OVERDUE
                if (record.getBorrowStatus() == BorrowRecord.BorrowStatus.ACTIVE) {
                    record.markAsOverdue();
                    borrowRecordRepository.save(record);
                }

                UserProfile userProfile = userProfileRepository.findByUserId(record.getMemberId()).orElse(null);
                updateOverdueFineForRecord(record, userProfile);
            } catch (Exception e) {
                log.error("Failed to process daily fine for record: {}", record.getId(), e);
            }
        }
        log.info("Completed daily overdue and fine calculation job");
    }
    
    private BigDecimal calculateOverdueFine(BorrowRecord record) {
        if (!record.isOverdue()) {
            return BigDecimal.ZERO;
        }
        
        int overdueDays = record.getOverdueDays();
        BorrowPolicy policy = borrowPolicyRepository.findAllByMemberTypeOrderByCreatedAtDesc(BorrowPolicy.MemberType.USER)
                .stream()
                .findFirst()
                .orElseGet(() -> borrowPolicyRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new PolicyNotFoundException("Default policy not found")));
        
        return policy.calculateOverdueFine(overdueDays);
    }
}
