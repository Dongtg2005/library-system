package com.lms.library.application.service;

import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Fine;
import com.lms.library.domain.repository.BorrowPolicyRepository;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.FineRepository;
import com.lms.library.application.dto.*;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.exception.PolicyNotFoundException;
import com.lms.library.domain.exception.BorrowLimitExceededException;
import com.lms.library.domain.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @Transactional
    public BorrowResponse createBorrowing(UUID memberId, BorrowPolicy.MemberType memberType, CreateBorrowRequest request) {
        log.info("Processing borrow request for member: {}", memberId);
        
        // Fetch Borrow Policy
        BorrowPolicy policy = borrowPolicyRepository.findByMemberType(memberType)
                .orElseThrow(() -> new PolicyNotFoundException("Borrow policy not found for member type: " + memberType));
        
        // Count Active Borrows
        int currentBorrows = borrowRecordRepository.countByMemberIdAndBorrowStatusIn(
                memberId, List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.PENDING_APPROVAL));
        
        // Validate
        if (!policy.canBorrowMoreBooks(currentBorrows)) {
            throw new BorrowLimitExceededException(
                    String.format("Member has %d active borrows, which meets or exceeds the limit of %d.", 
                                  currentBorrows, policy.getMaxBooksAllowed()));
        }
        
        // Create Borrow Record
        BorrowRecord record = BorrowRecord.builder()
                .memberId(memberId)
                .bookId(request.getBookId())
                .dueDate(LocalDate.now().plusDays(policy.getLoanPeriodDays()))
                .maxExtensions(policy.getMaxExtensions())
                .borrowStatus(BorrowRecord.BorrowStatus.ACTIVE)
                .build();
        
        record = borrowRecordRepository.save(record);
        
        // Calculate and create fine if needed (for overdue books)
        calculateOverdueFines(memberId);
        
        return BorrowResponse.from(record);
    }
    
    @Transactional
    public ReturnResponse processReturn(UUID memberId, ReturnRequest request) {
        log.info("Processing return for member: {}", memberId);
        
        BorrowRecord record = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
        
        if (!record.getMemberId().equals(memberId)) {
            throw new ForbiddenOperationException("Borrow record does not belong to this member");
        }
        
        // Calculate overdue fine if applicable
        BigDecimal overdueFine = calculateOverdueFine(record);
        
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
        }
        
        return ReturnResponse.builder()
                .borrowRecordId(record.getId())
                .returnDate(record.getReturnDate())
                .conditionOnReturn(record.getConditionOnReturn())
                .overdueFine(overdueFine)
                .build();
    }
    
    @Transactional
    public BorrowResponse extendLoan(UUID borrowRecordId, UUID memberId) {
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
    public List<BorrowResponse> getMemberBorrowHistory(UUID memberId) {
        List<BorrowRecord> records = borrowRecordRepository.findByMemberId(memberId);
        return records.stream()
                .map(BorrowResponse::from)
                .toList();
    }
    
    private void calculateOverdueFines(UUID memberId) {
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
        BorrowPolicy policy = borrowPolicyRepository.findByMemberType(BorrowPolicy.MemberType.STUDENT)
                .orElseThrow(() -> new PolicyNotFoundException("Default policy not found"));
        
        return policy.calculateOverdueFine(overdueDays);
    }
}
