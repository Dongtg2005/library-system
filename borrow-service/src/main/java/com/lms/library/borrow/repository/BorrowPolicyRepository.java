package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.BorrowPolicy;
import com.lms.library.borrow.entity.enums.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowPolicyRepository extends JpaRepository<BorrowPolicy, UUID> {
    Optional<BorrowPolicy> findByMemberType(MemberType memberType);
}
