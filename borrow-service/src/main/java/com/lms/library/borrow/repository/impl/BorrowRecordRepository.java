package com.lms.library.borrow.repository.impl;

import com.lms.library.borrow.entity.BorrowRecord;
import com.lms.library.borrow.entity.enums.BorrowStatus;
import com.lms.library.borrow.repository.IBorrowRecordRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID>, IBorrowRecordRepository {
    
    int countByMemberIdAndBorrowStatusIn(UUID memberId, Collection<BorrowStatus> borrowStatus);
}
