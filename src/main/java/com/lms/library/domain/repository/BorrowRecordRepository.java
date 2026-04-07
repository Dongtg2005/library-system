package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BorrowRecord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BorrowRecordRepository {
    BorrowRecord save(BorrowRecord borrowRecord);
    Optional<BorrowRecord> findById(UUID id);
    List<BorrowRecord> findByMemberId(UUID memberId);
    int countByMemberIdAndBorrowStatusIn(UUID memberId, Collection<BorrowRecord.BorrowStatus> borrowStatus);
    List<BorrowRecord> findByBookId(UUID bookId);
    List<BorrowRecord> findOverdueRecords();
    void deleteById(UUID id);
}
