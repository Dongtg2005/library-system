package com.lms.library.domain.repository;

import com.lms.library.domain.entity.BorrowPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowPolicyRepository extends JpaRepository<BorrowPolicy, UUID> {

    Optional<BorrowPolicy> findByMemberType(BorrowPolicy.MemberType memberType);

    List<BorrowPolicy> findAllByMemberTypeOrderByCreatedAtDesc(BorrowPolicy.MemberType memberType);
}
