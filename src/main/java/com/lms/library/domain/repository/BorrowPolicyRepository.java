package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BorrowPolicy;
import java.util.Optional;
import java.util.UUID;

public interface BorrowPolicyRepository {
    Optional<BorrowPolicy> findByMemberType(BorrowPolicy.MemberType memberType);
    Optional<BorrowPolicy> findById(UUID id);
    BorrowPolicy save(BorrowPolicy borrowPolicy);
    void deleteById(UUID id);
}
