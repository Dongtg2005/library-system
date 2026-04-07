package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.infrastructure.persistence.jpa.BorrowPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowPolicyJpaRepository extends JpaRepository<BorrowPolicyJpaEntity, UUID> {
    Optional<BorrowPolicyJpaEntity> findByMemberType(BorrowPolicy.MemberType memberType);
}
