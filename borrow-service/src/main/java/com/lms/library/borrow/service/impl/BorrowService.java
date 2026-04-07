package com.lms.library.borrow.service.impl;

import com.lms.library.borrow.dto.BorrowResponse;
import com.lms.library.borrow.dto.CreateBorrowRequest;
import com.lms.library.borrow.dto.ReturnRequest;
import com.lms.library.borrow.dto.ReturnResponse;
import com.lms.library.borrow.entity.BorrowEvent;
import com.lms.library.borrow.entity.BorrowPolicy;
import com.lms.library.borrow.entity.BorrowRecord;
import com.lms.library.borrow.entity.Fine;
import com.lms.library.borrow.entity.enums.*;
import com.lms.library.borrow.exception.BorrowLimitExceededException;
import com.lms.library.borrow.exception.DuplicateIdempotencyException;
import com.lms.library.borrow.exception.PolicyNotFoundException;
import com.lms.library.borrow.repository.IBorrowEventRepository;
import com.lms.library.borrow.repository.IBorrowPolicyRepository;
import com.lms.library.borrow.repository.IBorrowRecordRepository;
import com.lms.library.borrow.repository.IFineRepository;
import com.lms.library.borrow.service.IBorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService implements IBorrowService {

    private final IBorrowPolicyRepository borrowPolicyRepository;
    private final IBorrowRecordRepository borrowRecordRepository;
    private final IBorrowEventRepository borrowEventRepository;
    private final IFineRepository fineRepository;

    @Override
    @Transactional
    public BorrowResponse createBorrowing(UUID memberId, MemberType memberType, CreateBorrowRequest request, String idempotencyKey) {
        log.info("Processing borrow request for member: {} with idempotency key: {}", memberId, idempotencyKey);
        
        // 0. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (borrowEventRepository.existsBySagaId(idempotencyKey)) {
                log.warn("Duplicate request detected with idempotency key: {}", idempotencyKey);
                throw new DuplicateIdempotencyException("A request with this idempotency key has already been processed.");
            }
        }

        // Note: For absolute concurrency safety, consider distributed locking on `memberId` 
        // e.g. using Redis (Redisson) or `@Lock(LockModeType.PESSIMISTIC_WRITE)`
        
        // 1. Fetch Borrow Policy
        BorrowPolicy policy = borrowPolicyRepository.findByMemberType(memberType)
                .orElseThrow(() -> new PolicyNotFoundException("Borrow policy not found for member type: " + memberType));

        // 2. Count Active Borrows 
        // we check both PENDING_APPROVAL and ACTIVE to avoid limit bypass
        int currentBorrows = borrowRecordRepository.countByMemberIdAndBorrowStatusIn(
                memberId, List.of(BorrowStatus.ACTIVE, BorrowStatus.PENDING_APPROVAL));

        // 3. Validate
        if (currentBorrows >= policy.getMaxBooksAllowed()) {
            throw new BorrowLimitExceededException(
                    String.format("Member has %d active borrows, which meets or exceeds the limit of %d.", 
                                  currentBorrows, policy.getMaxBooksAllowed()));
        }

        // 4. Create Borrow Record (PENDING_APPROVAL)
        BorrowRecord record = BorrowRecord.builder()
                .memberId(memberId)
                .bookId(request.getBookId())
                // borrowDate & borrowTime initialized via defaults
                .dueDate(LocalDate.now().plusDays(policy.getLoanPeriodDays()))
                .maxExtensions(policy.getMaxExtensions())
                .borrowStatus(BorrowStatus.PENDING_APPROVAL)
                .build();
        
        record = borrowRecordRepository.save(record);

        // 5. Create BorrowEvent (Saga Trigger)
        String payload = String.format("{\"bookId\": \"%s\", \"idempotencyKey\": \"%s\"}", request.getBookId(), idempotencyKey);
        
        BorrowEvent event = BorrowEvent.builder()
                .borrowRecord(record)
                .eventType(EventType.BORROWED)
                .eventStatus(EventStatus.INITIATED)
                .memberId(memberId)
                .bookId(request.getBookId())
                .payload(payload)
                .sagaId(idempotencyKey) // Mapping idempotency to saga natively 
                .triggeredById(memberId)
                .build();
                
        borrowEventRepository.save(event);

        log.info("BorrowRecord [{}] created for member [{}] with status PENDING_APPROVAL", record.getId(), memberId);

        return BorrowResponse.builder()
                .recordId(record.getId())
                .memberId(memberId)
                .bookId(request.getBookId())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .status(record.getBorrowStatus())
                .build();

        // 6. Process return

    }

    @Override
    @Transactional
    public ReturnResponse processReturn(UUID memberId, ReturnRequest request) {
        log.info("Processing return request for member: {} for borrow record: {}", memberId, request.getBorrowRecordId());
        
        // 1. Find and validate borrow record
        BorrowRecord borrowRecord = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found: " + request.getBorrowRecordId()));
        
        // 2. Validate ownership
        if (!borrowRecord.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("This borrow record does not belong to the specified member");
        }
        
        // 3. Check current status
        if (borrowRecord.getBorrowStatus() == BorrowStatus.RETURNED) {
            throw new IllegalStateException("Book has already been returned");
        }
        
        if (borrowRecord.getBorrowStatus() != BorrowStatus.ACTIVE && borrowRecord.getBorrowStatus() != BorrowStatus.OVERDUE) {
            throw new IllegalStateException("Cannot return book with status: " + borrowRecord.getBorrowStatus());
        }
        
        // 4. Set return information
        LocalDate returnDate = LocalDate.now();
        ZonedDateTime returnTime = ZonedDateTime.now();
        
        borrowRecord.setReturnDate(returnDate);
        borrowRecord.setReturnTime(returnTime);
        borrowRecord.setConditionOnReturn(request.getConditionOnReturn());
        borrowRecord.setReturnNotes(request.getReturnNotes());
        borrowRecord.setBorrowStatus(BorrowStatus.RETURNED);
        
        borrowRecord = borrowRecordRepository.save(borrowRecord);
        
        // 5. Calculate overdue days and fine
        Integer overdueDays = calculateOverdueDays(borrowRecord.getDueDate(), returnDate);
        BigDecimal fineAmount = BigDecimal.ZERO;
        
        if (overdueDays > 0) {
            fineAmount = calculateFineAmount(borrowRecord.getMemberId(), overdueDays);
            
            // Create fine record if applicable
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                createFineRecord(borrowRecord, overdueDays, fineAmount);
            }
        }
        
        // 6. Create return event
        createReturnEvent(borrowRecord, memberId, overdueDays, fineAmount);
        
        // 7. Build response
        String message = buildReturnMessage(overdueDays, fineAmount);
        
        ReturnResponse response = ReturnResponse.builder()
                .borrowRecordId(borrowRecord.getId())
                .memberId(borrowRecord.getMemberId())
                .bookId(borrowRecord.getBookId())
                .borrowDate(borrowRecord.getBorrowDate())
                .dueDate(borrowRecord.getDueDate())
                .returnDate(returnDate)
                .returnTime(returnTime)
                .status(borrowRecord.getBorrowStatus())
                .conditionOnReturn(request.getConditionOnReturn())
                .overdueDays(overdueDays)
                .fineAmount(fineAmount)
                .processedSuccessfully(true)
                .message(message)
                .build();
        
        log.info("Book return processed successfully for record: {}, overdue days: {}, fine: {}", 
                borrowRecord.getId(), overdueDays, fineAmount);
        
        return response;
    }
    
    private Integer calculateOverdueDays(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate.isAfter(dueDate)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
        }
        return 0;
    }
    
    private BigDecimal calculateFineAmount(UUID memberId, Integer overdueDays) {
        // Get member's borrow policy to determine fine rate
        // For now, we'll use a default approach since we don't have member type here
        // In a real implementation, you might need to fetch member type from user service
        BorrowPolicy defaultPolicy = borrowPolicyRepository.findByMemberType(MemberType.STUDENT)
                .orElseThrow(() -> new PolicyNotFoundException("Default borrow policy not found"));
        
        BigDecimal dailyFine = defaultPolicy.getFinePerDay();
        BigDecimal totalFine = dailyFine.multiply(BigDecimal.valueOf(overdueDays));
        
        // Apply max fine per book if set
        if (defaultPolicy.getMaxFinePerBook() != null) {
            totalFine = totalFine.min(defaultPolicy.getMaxFinePerBook());
        }
        
        return totalFine.setScale(2, RoundingMode.HALF_UP);
    }
    
    private void createFineRecord(BorrowRecord borrowRecord, Integer overdueDays, BigDecimal fineAmount) {
        // Get the policy to set correct daily rate
        BorrowPolicy policy = borrowPolicyRepository.findByMemberType(MemberType.STUDENT)
                .orElseThrow(() -> new PolicyNotFoundException("Default borrow policy not found"));
        
        Fine fine = Fine.builder()
                .borrowRecord(borrowRecord)
                .memberId(borrowRecord.getMemberId())
                .fineType(FineType.OVERDUE)
                .amount(fineAmount)
                .dailyRate(policy.getFinePerDay())
                .daysOverdue(overdueDays)
                .reason(String.format("Overdue return for %d days", overdueDays))
                .status(FineStatus.PENDING)
                .build();
        
        fineRepository.save(fine);
        log.info("Fine record created for borrow record: {}, amount: {}", borrowRecord.getId(), fineAmount);
    }
    
    private void createReturnEvent(BorrowRecord borrowRecord, UUID memberId, Integer overdueDays, BigDecimal fineAmount) {
        String payload = String.format(
                "{\"borrowRecordId\": \"%s\", \"overdueDays\": %d, \"fineAmount\": %s}", 
                borrowRecord.getId(), overdueDays, fineAmount);
        
        BorrowEvent event = BorrowEvent.builder()
                .borrowRecord(borrowRecord)
                .eventType(EventType.RETURNED)
                .eventStatus(EventStatus.COMPLETED)
                .memberId(memberId)
                .bookId(borrowRecord.getBookId())
                .payload(payload)
                .triggeredById(memberId)
                .build();
                
        borrowEventRepository.save(event);
    }
    
    private String buildReturnMessage(Integer overdueDays, BigDecimal fineAmount) {
        if (overdueDays > 0) {
            return String.format("Book returned successfully with %d days overdue. Fine: $%s", overdueDays, fineAmount);
        } else {
            return "Book returned successfully on time";
        }
    }
}
