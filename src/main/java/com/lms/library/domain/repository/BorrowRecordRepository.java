package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {

    List<BorrowRecord> findByMemberId(Long memberId);

    long countByMemberId(Long memberId);

    long countByBorrowStatus(BorrowRecord.BorrowStatus status);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    int countByMemberIdAndBorrowStatusIn(Long memberId, Collection<BorrowRecord.BorrowStatus> borrowStatus);

    int countByMemberIdAndCreatedAtBetweenAndBorrowStatusIn(Long memberId,
                                                            java.time.LocalDateTime start,
                                                            java.time.LocalDateTime end,
                                                            Collection<BorrowRecord.BorrowStatus> statuses);

    List<BorrowRecord> findByBookId(UUID bookId);

    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowStatus = 'ACTIVE' AND br.dueDate < CURRENT_DATE")
    List<BorrowRecord> findOverdueRecords();

    @Query(value = "SELECT * FROM borrow_records br WHERE br.borrow_status = 'ACTIVE' AND br.due_date >= CURRENT_DATE AND br.due_date <= CURRENT_DATE + INTERVAL '3 days'", nativeQuery = true)
    List<BorrowRecord> findDueSoonRecords();

    List<BorrowRecord> findByBorrowStatus(BorrowRecord.BorrowStatus status);

    Page<BorrowRecord> findByBorrowStatus(BorrowRecord.BorrowStatus status, Pageable pageable);

    Optional<BorrowRecord> findByMemberIdAndBookIdAndBorrowStatusIn(Long memberId, UUID bookId, Collection<BorrowRecord.BorrowStatus> statuses);

    // Check if user has ever borrowed this book (ACTIVE or RETURNED)
    boolean existsByMemberIdAndBookIdAndBorrowStatusIn(Long memberId, UUID bookId, Collection<BorrowRecord.BorrowStatus> statuses);
}
