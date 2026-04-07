package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Fine;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FineRepository {
    Fine save(Fine fine);
    Optional<Fine> findById(UUID id);
    List<Fine> findByBorrowRecordId(UUID borrowRecordId);
    List<Fine> findByMemberId(UUID memberId);
    List<Fine> findByStatus(Fine.FineStatus status);
    void deleteById(UUID id);
}
