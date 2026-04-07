package com.lms.library.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateIdempotencyException extends RuntimeException {
    
    public DuplicateIdempotencyException(String message) {
        super(message);
    }
}
