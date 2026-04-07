package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.Fine;
import com.lms.library.borrow.entity.enums.FineStatus;
import com.lms.library.borrow.entity.enums.FineType;

import java.util.List;
import java.util.UUID;

public interface IFineRepository {
    
    List<Fine> findByBorrowRecordId(UUID borrowRecordId);
    
    List<Fine> findByMemberIdAndStatus(UUID memberId, FineStatus status);
    
    List<Fine> findByBorrowRecordIdAndFineType(UUID borrowRecordId, FineType fineType);
    
    int countByBorrowRecordIdAndStatus(UUID borrowRecordId, FineStatus status);
    
    Double sumUnpaidAmountByBorrowRecordId(UUID borrowRecordId, FineStatus status);
    
    Fine save(Fine fine);
}
