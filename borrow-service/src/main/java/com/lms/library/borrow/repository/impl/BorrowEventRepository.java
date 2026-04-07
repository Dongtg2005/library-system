package com.lms.library.borrow.repository.impl;

import com.lms.library.borrow.entity.BorrowEvent;
import com.lms.library.borrow.repository.IBorrowEventRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BorrowEventRepository extends JpaRepository<BorrowEvent, UUID>, IBorrowEventRepository {
    
    boolean existsBySagaId(String sagaId);
}
