package com.lms.library.borrow.service;

import com.lms.library.borrow.dto.BorrowResponse;
import com.lms.library.borrow.dto.CreateBorrowRequest;
import com.lms.library.borrow.dto.ReturnRequest;
import com.lms.library.borrow.dto.ReturnResponse;
import com.lms.library.borrow.entity.enums.MemberType;

import java.util.UUID;

public interface IBorrowService {
    BorrowResponse createBorrowing(UUID memberId, MemberType memberType, CreateBorrowRequest request, String idempotencyKey);
    ReturnResponse processReturn(UUID memberId, ReturnRequest request);
}
