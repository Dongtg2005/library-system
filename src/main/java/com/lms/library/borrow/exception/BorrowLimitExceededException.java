package com.lms.library.borrow.exception;

public class BorrowLimitExceededException extends RuntimeException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }
}
