package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.infrastructure.persistence.jpa.BorrowRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowRecordJpaRepository extends JpaRepository<BorrowRecordJpaEntity, UUID> {
    List<BorrowRecordJpaEntity> findByMemberId(UUID memberId);
    
    @Query("SELECT COUNT(br) FROM BorrowRecordJpaEntity br WHERE br.memberId = :memberId AND br.borrowStatus IN :statuses")
    int countByMemberIdAndBorrowStatusIn(@Param("memberId") UUID memberId, @Param("statuses") Collection<BorrowRecord.BorrowStatus> statuses);
    
    List<BorrowRecordJpaEntity> findByBookId(UUID bookId);
    
    @Query("SELECT br FROM BorrowRecordJpaEntity br WHERE br.borrowStatus = 'ACTIVE' AND br.dueDate < CURRENT_DATE")
    List<BorrowRecordJpaEntity> findOverdueRecords();
}
