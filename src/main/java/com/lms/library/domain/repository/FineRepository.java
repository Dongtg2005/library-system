package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FineRepository extends JpaRepository<Fine, UUID> {

    List<Fine> findByBorrowRecordId(UUID borrowRecordId);

    List<Fine> findByMemberId(Long memberId);

    List<Fine> findByStatus(Fine.FineStatus status);
}
