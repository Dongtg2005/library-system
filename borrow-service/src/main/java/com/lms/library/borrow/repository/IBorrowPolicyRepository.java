package com.lms.library.borrow.repository;

import com.lms.library.borrow.entity.BorrowPolicy;
import com.lms.library.borrow.entity.enums.MemberType;

import java.util.Optional;

public interface IBorrowPolicyRepository {
    
    Optional<BorrowPolicy> findByMemberType(MemberType memberType);
}
