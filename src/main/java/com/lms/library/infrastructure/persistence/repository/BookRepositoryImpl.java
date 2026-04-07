package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.Book;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.infrastructure.persistence.jpa.BookJpaEntity;
import com.lms.library.infrastructure.persistence.mapper.BookPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {
    
    private final BookJpaRepository bookJpaRepository;
    private final BookPersistenceMapper mapper;
    
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookJpaRepository.findByIsbn(isbn)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Book> findById(UUID id) {
        return bookJpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByIsbn(String isbn) {
        return bookJpaRepository.existsByIsbn(isbn);
    }
    
    @Override
    public Book save(Book book) {
        BookJpaEntity entity = mapper.toJpaEntity(book);
        BookJpaEntity saved = bookJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void deleteById(UUID id) {
        bookJpaRepository.deleteById(id);
    }
    
    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookJpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
    
    @Override
    public Page<Book> searchBooks(String title, String author, String category, Book.BookStatus status, Pageable pageable) {
        return bookJpaRepository.searchBooks(title, author, category, status, pageable)
                .map(mapper::toDomain);
    }
}
