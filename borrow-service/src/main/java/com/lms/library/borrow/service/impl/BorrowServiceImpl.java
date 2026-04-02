package com.lms.library.borrow.service.impl;

import com.lms.library.borrow.dto.BorrowResponse;
import com.lms.library.borrow.dto.CreateBorrowRequest;
import com.lms.library.borrow.entity.BorrowEvent;
import com.lms.library.borrow.entity.BorrowPolicy;
import com.lms.library.borrow.entity.BorrowRecord;
import com.lms.library.borrow.entity.enums.BorrowStatus;
import com.lms.library.borrow.entity.enums.EventStatus;
import com.lms.library.borrow.entity.enums.EventType;
import com.lms.library.borrow.entity.enums.MemberType;
import com.lms.library.borrow.exception.BorrowLimitExceededException;
import com.lms.library.borrow.exception.DuplicateIdempotencyException;
import com.lms.library.borrow.exception.PolicyNotFoundException;
import com.lms.library.borrow.repository.BorrowEventRepository;
import com.lms.library.borrow.repository.BorrowPolicyRepository;
import com.lms.library.borrow.repository.BorrowRecordRepository;
import com.lms.library.borrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowPolicyRepository borrowPolicyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowEventRepository borrowEventRepository;

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
    }
}
