package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
// Implement JpaSpecificationExecutor để dùng được JPA Specification Dynamic Query
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    long countByCreatedAtBefore(java.time.LocalDateTime date);

    // Không cần dùng query hardcode lằng nhằng như thế này nữa, nhưng tạm thời comment/giữ lại do logic khác có thể gọi tới
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

       @Query("SELECT b FROM Book b WHERE b.status <> com.lms.library.domain.entity.Book$BookStatus.ARCHIVED ORDER BY COALESCE(b.borrowedQuantity, 0) DESC, b.updatedAt DESC")
       List<Book> findTopBorrowedBooks(Pageable pageable);
}
