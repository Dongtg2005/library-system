package com.lms.library.application.service;

import com.lms.library.domain.entity.Book;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.application.dto.*;
import com.lms.library.domain.exception.DuplicateResourceException;
import com.lms.library.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookManagementService {
    
    private final BookRepository bookRepository;
    
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        log.info("Creating book with ISBN: {}", request.getIsbn());
        
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book with ISBN [" + request.getIsbn() + "] already exists!"
            );
        }
        
        Book newBook = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .category(request.getCategory())
                .totalQuantity(request.getTotalQuantity())
                .availableQty(request.getTotalQuantity())
                .status(Book.BookStatus.AVAILABLE)
                .build();
        
        Book savedBook = bookRepository.save(newBook);
        return BookResponse.from(savedBook);
    }
    
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        log.info("Getting all books with pagination");
        return bookRepository.findAll(pageable)
                .map(BookResponse::from);
    }
    
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(
            String title,
            String author,
            String category,
            String status,
            Pageable pageable
    ) {
        log.info("Searching books - title: {}, author: {}, category: {}, status: {}",
                title, author, category, status);
        
        Book.BookStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Book.BookStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Invalid status. Only accept: AVAILABLE, OUT_OF_STOCK, ARCHIVED"
                );
            }
        }
        
        return bookRepository
                .searchBooks(title, author, category, statusEnum, pageable)
                .map(BookResponse::from);
    }
    
    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {
        log.info("Getting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with ID: " + id
                ));
        
        return BookResponse.from(book);
    }
    
    @Transactional
    public BookResponse updateBook(UUID id, BookUpdateRequest request) {
        log.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found with ID: " + id)
                );
        
        // Update fields
        if (request.getTitle() != null) {
            existingBook.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            existingBook.setAuthor(request.getAuthor());
        }
        if (request.getCategory() != null) {
            existingBook.setCategory(request.getCategory());
        }
        if (request.getTotalQuantity() != null) {
            existingBook.setTotalQuantity(request.getTotalQuantity());
            // Adjust available quantity if needed
            if (request.getTotalQuantity() < existingBook.getAvailableQty()) {
                existingBook.setAvailableQty(request.getTotalQuantity());
            }
        }
        
        Book savedBook = bookRepository.save(existingBook);
        return BookResponse.from(savedBook);
    }
    
    @Transactional
    public void deleteBook(UUID id) {
        log.info("Archiving book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found with ID: " + id)
                );
        
        existingBook.archive();
        bookRepository.save(existingBook);
        
        log.info("Book {} has been archived", id);
    }
    
    @Transactional
    public void borrowBook(UUID id) {
        log.info("Borrowing book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        book.borrowBook();
        bookRepository.save(book);
    }
    
    @Transactional
    public void returnBook(UUID id) {
        log.info("Returning book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        book.returnBook();
        bookRepository.save(book);
    }
}
