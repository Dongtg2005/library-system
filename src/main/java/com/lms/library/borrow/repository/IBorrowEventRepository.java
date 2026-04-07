package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.BorrowEvent;

public interface IBorrowEventRepository {
    
    boolean existsBySagaId(String sagaId);
    
    BorrowEvent save(BorrowEvent borrowEvent);
}
