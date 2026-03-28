package com.lms.library.book.repository;


import org.springframework.data.jpa.repository.JpaRepository;
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
}
