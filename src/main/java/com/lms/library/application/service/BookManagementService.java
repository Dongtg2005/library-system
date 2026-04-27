package com.lms.library.application.service;

import com.lms.library.domain.entity.Book;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.CategoryRepository;
import com.lms.library.application.dto.*;
import com.lms.library.domain.exception.DuplicateResourceException;
import com.lms.library.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookManagementService {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    
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
                .totalQuantity(request.getTotalQuantity())
                .availableQty(request.getTotalQuantity())
                .status(Book.BookStatus.AVAILABLE)
                .categories(request.getCategoryIds() != null ? categoryRepository.findAllById(request.getCategoryIds()) : new java.util.ArrayList<>())
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
        if (request.getCategoryIds() != null) {
             existingBook.setCategories(categoryRepository.findAllById(request.getCategoryIds()));
        }
        if (request.getTotalQuantity() != null) {
            int oldTotal = existingBook.getTotalQuantity();
            int oldAvailable = existingBook.getAvailableQty();
            int borrowed = oldTotal - oldAvailable; // Books currently borrowed

            existingBook.setTotalQuantity(request.getTotalQuantity());
            // Recalculate available: newTotal - borrowed
            existingBook.setAvailableQty(request.getTotalQuantity() - borrowed);

            // Update status if book is now available
            if (existingBook.getAvailableQty() > 0) {
                existingBook.setStatus(Book.BookStatus.AVAILABLE);
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
    
    @Transactional
    public CoverResponse uploadCover(UUID bookId, MultipartFile file) {
        log.info("Uploading cover for book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
                
        // Delete old cover if exists
        if (book.getCoverImageUrl() != null) {
            fileStorageService.deleteFileByUrl(book.getCoverImageUrl());
        }
        
        String coverUrl = fileStorageService.storeCoverImage(file);
        book.setCoverImageUrl(coverUrl);
        bookRepository.save(book);
        
        return CoverResponse.builder()
                .success(true)
                .coverUrl(coverUrl)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    @Transactional
    public CoverResponse uploadCoverFromUrl(UUID bookId, String url) {
        log.info("Uploading remote cover for book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
                
        if (book.getCoverImageUrl() != null) {
            fileStorageService.deleteFileByUrl(book.getCoverImageUrl());
        }
        
        String coverUrl = fileStorageService.storeCoverImageFromUrl(url);
        book.setCoverImageUrl(coverUrl);
        bookRepository.save(book);
        
        return CoverResponse.builder()
                .success(true)
                .coverUrl(coverUrl)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    @Transactional
    public void deleteCover(UUID bookId) {
        log.info("Deleting cover for book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
                
        if (book.getCoverImageUrl() != null) {
            fileStorageService.deleteFileByUrl(book.getCoverImageUrl());
            book.setCoverImageUrl(null);
            bookRepository.save(book);
        }
    }
}
