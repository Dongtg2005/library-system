package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BorrowRecord;
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

    int countByMemberIdAndBorrowStatusIn(Long memberId, Collection<BorrowRecord.BorrowStatus> borrowStatus);

    List<BorrowRecord> findByBookId(UUID bookId);

    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowStatus = 'ACTIVE' AND br.dueDate < CURRENT_DATE")
    List<BorrowRecord> findOverdueRecords();

    List<BorrowRecord> findByBorrowStatus(BorrowRecord.BorrowStatus status);

    Optional<BorrowRecord> findByMemberIdAndBookIdAndBorrowStatusIn(Long memberId, UUID bookId, Collection<BorrowRecord.BorrowStatus> statuses);
}
