package com.lms.library.book.repository;

import com.lms.library.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lms.library.book.entity.Book;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    // Tự động generate câu query tìm sách theo mã vạch
    Optional<Book> findByIsbn(String isbn);
    
    // Tự động kiểm tra sách có tồn tại mã vạch này chưa
    boolean existsByIsbn(String isbn);

    // CÂU LỆNH TÌM KIẾM NÀY 👇
    // Kỹ thuật: Nếu title truyền vào là null thì bỏ qua điều kiện đó, nếu có thì tìm gần đúng (LIKE).
    @Query("""
       SELECT b FROM Book b WHERE
       LOWER(COALESCE(b.title, '')) LIKE LOWER(CONCAT('%', COALESCE(:title, ''), '%'))
       AND LOWER(COALESCE(b.author, '')) LIKE LOWER(CONCAT('%', COALESCE(:author, ''), '%'))
       AND LOWER(COALESCE(b.category, '')) LIKE LOWER(CONCAT('%', COALESCE(:category, ''), '%'))
       AND (:status IS NULL OR b.status = :status)
       """)
       Page<Book> searchBooks(
              @Param("title") String title,
              @Param("author") String author,
              @Param("category") String category,
              @Param("status") Book.BookStatus status,
              Pageable pageable
       );
}

