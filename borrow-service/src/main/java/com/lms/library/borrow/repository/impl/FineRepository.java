package com.lms.library.borrow.repository.impl;

import com.lms.library.borrow.entity.Fine;
import com.lms.library.borrow.entity.enums.FineStatus;
import com.lms.library.borrow.entity.enums.FineType;
import com.lms.library.borrow.repository.IFineRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FineRepository extends JpaRepository<Fine, UUID>, IFineRepository {
    
    List<Fine> findByBorrowRecordId(UUID borrowRecordId);
    
    List<Fine> findByMemberIdAndStatus(UUID memberId, FineStatus status);
    
    List<Fine> findByBorrowRecordIdAndFineType(UUID borrowRecordId, FineType fineType);
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.borrowRecord.id = :borrowRecordId AND f.status = :status")
    int countByBorrowRecordIdAndStatus(@Param("borrowRecordId") UUID borrowRecordId, @Param("status") FineStatus status);
    
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.borrowRecord.id = :borrowRecordId AND f.status = :status")
    Double sumUnpaidAmountByBorrowRecordId(@Param("borrowRecordId") UUID borrowRecordId, @Param("status") FineStatus status);
}
