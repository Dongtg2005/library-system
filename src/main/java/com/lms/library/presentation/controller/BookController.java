package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.BookManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookManagementService bookManagementService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        log.info("Creating book with ISBN: {}", request.getIsbn());
        BookResponse response = bookManagementService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(Pageable pageable) {
        log.info("Getting all books with pagination");
        Page<BookResponse> response = bookManagementService.getAllBooks(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        log.info("Searching books with filters");
        Page<BookResponse> response = bookManagementService.searchBooks(title, author, category, status, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        log.info("Getting book with ID: {}", id);
        BookResponse response = bookManagementService.getBookById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookUpdateRequest request) {
        log.info("Updating book with ID: {}", id);
        BookResponse response = bookManagementService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        log.info("Deleting book with ID: {}", id);
        bookManagementService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/borrow")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Void> borrowBook(@PathVariable UUID id) {
        log.info("Borrowing book with ID: {}", id);
        bookManagementService.borrowBook(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Void> returnBook(@PathVariable UUID id) {
        log.info("Returning book with ID: {}", id);
        bookManagementService.returnBook(id);
        return ResponseEntity.ok().build();
    }
}
