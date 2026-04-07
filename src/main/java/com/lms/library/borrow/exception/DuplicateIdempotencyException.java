package com.lms.library.borrow.exception;

public class DuplicateIdempotencyException extends RuntimeException {
    public DuplicateIdempotencyException(String message) {
        super(message);
    }
}
