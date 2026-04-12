package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:category IS NULL OR EXISTS (SELECT 1 FROM b.categories c WHERE c.name = :category)) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("category") String category,
                           @Param("status") Book.BookStatus status,
                           Pageable pageable);
}
