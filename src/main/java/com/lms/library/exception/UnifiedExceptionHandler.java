package com.lms.library.exception;

import com.lms.library.auth.exception.EmailAlreadyExistsException;
import com.lms.library.auth.exception.InvalidCredentialsException;
import com.lms.library.book.exception.DuplicateResourceException;
import com.lms.library.book.exception.ResourceNotFoundException;
import com.lms.library.borrow.exception.BorrowLimitExceededException;
import com.lms.library.borrow.exception.DuplicateIdempotencyException;
import com.lms.library.borrow.exception.PolicyNotFoundException;
import com.lms.library.user.exception.ForbiddenOperationException;
import com.lms.library.user.exception.UserAlreadyExistsException;
import com.lms.library.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified global exception handler for all modules in monolith
 * Consolidates exception handling from: auth, user, book, and borrow services
 */
@RestControllerAdvice
@Slf4j
public class UnifiedExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnifiedExceptionHandler.class);

    /**
     * Handle EmailAlreadyExistsException (Auth module)
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, WebRequest request) {
        log.warn("Email already exists: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle InvalidCredentialsException (Auth module)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle UserNotFoundException (User module)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle UserAlreadyExistsException (User module)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(
            UserAlreadyExistsException ex, WebRequest request) {
        log.warn("User already exists: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "User Already Exists",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle ForbiddenOperationException (User module)
     */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenOperation(
            ForbiddenOperationException ex, WebRequest request) {
        log.warn("Forbidden operation: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden Operation",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle DuplicateResourceException (Book module)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Duplicate Resource",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle ResourceNotFoundException (Book module)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle BorrowLimitExceededException (Borrow module)
     */
    @ExceptionHandler(BorrowLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleBorrowLimitExceeded(
            BorrowLimitExceededException ex, WebRequest request) {
        log.warn("Borrow limit exceeded: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Borrow Limit Exceeded",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle PolicyNotFoundException (Borrow module)
     */
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePolicyNotFound(
            PolicyNotFoundException ex, WebRequest request) {
        log.warn("Policy not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Policy Not Found",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle DuplicateIdempotencyException (Borrow module)
     */
    @ExceptionHandler(DuplicateIdempotencyException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateIdempotency(
            DuplicateIdempotencyException ex, WebRequest request) {
        log.warn("Duplicate idempotency: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Duplicate Request",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle MethodArgumentNotValidException (Validation errors)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation error: {}", errors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "Invalid input data");
        body.put("validationErrors", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Argument",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid State",
                ex.getMessage(),
                request
        );
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request
        );
    }

    /**
     * Helper method to build error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, status);
    }
}
