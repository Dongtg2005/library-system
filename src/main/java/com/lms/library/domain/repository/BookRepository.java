package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findById(UUID id);
    boolean existsByIsbn(String isbn);
    Book save(Book book);
    void deleteById(UUID id);
    Page<Book> findAll(Pageable pageable);
    Page<Book> searchBooks(String title, String author, String category, Book.BookStatus status, Pageable pageable);
}
