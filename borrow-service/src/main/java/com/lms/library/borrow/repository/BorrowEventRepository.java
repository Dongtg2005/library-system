package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.BorrowEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BorrowEventRepository extends JpaRepository<BorrowEvent, UUID> {
    boolean existsBySagaId(String sagaId);
}
