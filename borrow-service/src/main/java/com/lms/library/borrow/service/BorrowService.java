package com.lms.library.borrow.service;

import com.lms.library.borrow.dto.BorrowResponse;
import com.lms.library.borrow.dto.CreateBorrowRequest;
import com.lms.library.borrow.entity.enums.MemberType;

import java.util.UUID;

public interface BorrowService {
    BorrowResponse createBorrowing(UUID memberId, MemberType memberType, CreateBorrowRequest request, String idempotencyKey);
}
