package com.lms.library.book.controller;

import com.lms.library.book.dto.request.BookCreateRequest;
import com.lms.library.book.dto.request.BookUpdateRequest;
import com.lms.library.book.dto.response.BookResponse;
import com.lms.library.book.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // Đánh dấu đây là REST API → trả JSON
@RequestMapping("/api/v1/books") // Base URL chung cho tất cả API
@RequiredArgsConstructor // Lombok tự inject BookService qua constructor
public class BookController {

    // Inject Service để xử lý business logic
    private final BookService bookService;

    // ================= CREATE BOOK =================
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request
            // @Valid → kích hoạt validation (NotBlank, Min...)
    ) {
        // Gọi service để tạo sách
        BookResponse response = bookService.createBook(request);

        // Trả về HTTP 201 (Created) + dữ liệu sách vừa tạo
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================= SEARCH + FILTER + PAGINATION =================
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(

            // Tìm gần đúng theo title (LIKE %title%)
            @RequestParam(required = false) String title,

            // Tìm gần đúng theo author
            @RequestParam(required = false) String author,

            // Filter theo category (có thể LIKE)
            @RequestParam(required = false) String category,

            // Truyền String để tránh crash Enum → convert ở Service
            @RequestParam(required = false) String status,

            // Pagination + Sort (Spring tự parse từ URL)
            // Ví dụ: ?page=0&size=10&sort=title,asc
            @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {

        // Gọi service xử lý search + filter + pagination
        Page<BookResponse> result =
                bookService.searchBooks(title, author, category, status, pageable);

        // Trả về danh sách có phân trang
        return ResponseEntity.ok(result);
    }

    // ================= GET ALL (KHÔNG FILTER) =================
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(

            // Pagination mặc định
            @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {

        // Gọi service riêng → đúng design (không hack searchBooks)
        Page<BookResponse> result = bookService.getAllBooks(pageable);

        return ResponseEntity.ok(result);
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable("id") UUID id // lấy id từ URL
    ) {

        // Gọi service tìm sách theo ID
        BookResponse book = bookService.getBookById(id);

        return ResponseEntity.ok(book);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable("id") UUID id,

            @Valid @RequestBody BookUpdateRequest request
            // validate dữ liệu update
    ) {

        // Gọi service update sách
        BookResponse updatedBook = bookService.updateBook(id, request);

        return ResponseEntity.ok(updatedBook);
    }

    // ================= DELETE (SOFT DELETE) =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable("id") UUID id
    ) {

        // Không xóa DB → chỉ đổi status = ARCHIVED
        bookService.deleteBook(id);

        // 204 No Content → thành công nhưng không trả dữ liệu
        return ResponseEntity.noContent().build();
    }

    // ================= BORROW (GIẢM SỐ LƯỢNG) =================
    @PostMapping("/{id}/borrow")
    public ResponseEntity<Void> borrowBook(
            @PathVariable UUID id
    ) {

        // Gọi service:
        // - check còn sách không
        // - giảm availableQty
        // - update status nếu hết
        bookService.borrowBook(id);

        // 200 OK → thực hiện thành công
        return ResponseEntity.ok().build();
    }

    // ================= RETURN (TĂNG SỐ LƯỢNG) =================
    @PostMapping("/{id}/return")
    public ResponseEntity<Void> returnBook(
            @PathVariable UUID id
    ) {

        // Gọi service:
        // - tăng availableQty
        // - update status = AVAILABLE
        bookService.returnBook(id);

        return ResponseEntity.ok().build();
    }
}