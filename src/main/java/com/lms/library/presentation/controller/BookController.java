package com.lms.library.presentation.controller;

import com.lms.library.application.dto.*;
import com.lms.library.application.service.BookManagementService;
import com.lms.library.application.service.BookSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "Book Management", description = "Operations related to books")
public class BookController {
    
    private final BookManagementService bookManagementService;
    private final BookSearchService bookSearchService;
    
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
            // q là keyword tổng hợp cho title, author. Dùng cho ô search bar trên React.
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String isbn,
            @PageableDefault(size = 12) Pageable pageable) {
        log.info("Advanced searching books with filters");

        // Khả năng tương thích ngược (Backward Compatibility)
        String keyword = (q != null && !q.isBlank()) ? q : (title != null ? title : author);

        Page<BookResponse> response = bookSearchService.advancedSearch(
                keyword, isbn, category, status, pageable
        );
        return ResponseEntity.ok(response);
    }
    
    // API nhẹ, chuyên phục vụ Search Dropdown Suggestion.
    @GetMapping("/autocomplete")
    public ResponseEntity<List<BookResponse>> autocompleteBooks(
            @RequestParam(name = "q") String q) {
        log.info("Autocompleting for keyword: {}", q);
        List<BookResponse> response = bookSearchService.autocomplete(q);
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
