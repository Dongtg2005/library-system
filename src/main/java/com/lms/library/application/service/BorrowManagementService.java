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
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getAllBorrows(BorrowRecord.BorrowStatus status, Pageable pageable) {
        log.info("Fetching all borrows with status: {}", status);
        Page<BorrowRecord> records;
        if (status != null) {
            records = borrowRecordRepository.findByBorrowStatus(status, pageable);
        } else {
            records = borrowRecordRepository.findAll(pageable);
        }
        return records.map(BorrowResponse::from);
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

        // 3. Check borrow limit
        int currentBorrows = borrowRecordRepository.countByMemberIdAndBorrowStatusIn(
                memberId, List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.PENDING_APPROVAL));
        if (!policy.canBorrowMoreBooks(currentBorrows)) {
            throw new BorrowLimitExceededException(
                    String.format("You have reached the borrow limit. Current: %d, Maximum allowed: %d",
                                  currentBorrows, policy.getMaxBooksAllowed()));
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
        return BorrowResponse.from(record);
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

        log.info("Borrow request {} approved", borrowRecordId);
        return BorrowResponse.from(record);
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

        log.info("Borrow request {} rejected with reason: {}", borrowRecordId, reason);
        return BorrowResponse.from(record);
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

        // Return book to inventory
        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.returnBook();
        bookRepository.save(book);

        // Auto-fulfill first reservation if available
        if (book.isAvailable()) {
            reservationService.autoFulfillFirstReservation(book.getId());
        }

        // Decrement user's borrowed count
        UserProfile userProfile = userProfileRepository.findByUserId(memberId)
                .orElse(null);
        if (userProfile != null) {
            userProfile.decrementBorrowedCount();
            userProfileRepository.save(userProfile);
        }

        // Mark book as returned
        record.returnBook(request.getConditionOnReturn());
        borrowRecordRepository.save(record);

        // Create fine record if overdue
        if (overdueFine.compareTo(BigDecimal.ZERO) > 0) {
            Fine fine = Fine.builder()
                    .borrowRecordId(record.getId())
                    .memberId(memberId)
                    .amount(overdueFine)
                    .fineType(Fine.FineType.OVERDUE)
                    .status(Fine.FineStatus.PENDING)
                    .reason("Book returned overdue by " + record.getOverdueDays() + " days")
                    .build();

            fineRepository.save(fine);

            // Update user's outstanding fines
            if (userProfile != null) {
                userProfile.setOutstandingFines(
                    userProfile.getOutstandingFines().add(overdueFine));
                userProfileRepository.save(userProfile);
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
        
        return BorrowResponse.from(record);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowResponse> getMemberBorrowHistory(Long memberId) {
        List<BorrowRecord> records = borrowRecordRepository.findByMemberId(memberId);
        return records.stream()
                .map(BorrowResponse::from)
                .toList();
    }
    
    private void calculateOverdueFines(Long memberId) {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByMemberId(memberId)
                .stream()
                .filter(BorrowRecord::isOverdue)
                .toList();
        
        for (BorrowRecord record : overdueRecords) {
            BigDecimal fineAmount = calculateOverdueFine(record);
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                Fine fine = Fine.builder()
                        .borrowRecordId(record.getId())
                        .memberId(memberId)
                        .amount(fineAmount)
                        .fineType(Fine.FineType.OVERDUE)
                        .status(Fine.FineStatus.PENDING)
                        .reason("Book overdue by " + record.getOverdueDays() + " days")
                        .build();
                
                fineRepository.save(fine);
            }
        }
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
