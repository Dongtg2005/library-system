package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.BorrowRecord;
import com.lms.library.borrow.entity.enums.BorrowStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IBorrowRecordRepository {
    
    int countByMemberIdAndBorrowStatusIn(UUID memberId, Collection<BorrowStatus> borrowStatus);
    
    BorrowRecord save(BorrowRecord borrowRecord);
    
    Optional<BorrowRecord> findById(UUID id);
}
