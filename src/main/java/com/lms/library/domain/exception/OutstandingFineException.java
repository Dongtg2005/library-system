package com.lms.library.domain.exception;

public class OutstandingFineException extends RuntimeException {
    public OutstandingFineException(String message) {
        super(message);
    }
}
