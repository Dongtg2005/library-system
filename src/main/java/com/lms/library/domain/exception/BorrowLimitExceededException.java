package com.lms.library.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BorrowLimitExceededException extends RuntimeException {
    
    public BorrowLimitExceededException(String message) {
        super(message);
    }
}
