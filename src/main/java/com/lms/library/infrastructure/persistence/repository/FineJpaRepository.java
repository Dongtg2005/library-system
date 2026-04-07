package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.Fine;
import com.lms.library.infrastructure.persistence.jpa.FineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FineJpaRepository extends JpaRepository<FineJpaEntity, UUID> {
    List<FineJpaEntity> findByBorrowRecordId(UUID borrowRecordId);
    List<FineJpaEntity> findByMemberId(UUID memberId);
    List<FineJpaEntity> findByStatus(Fine.FineStatus status);
}
