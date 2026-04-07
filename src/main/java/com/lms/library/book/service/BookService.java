package com.lms.library.book.service;

import com.lms.library.book.dto.request.BookCreateRequest;
import com.lms.library.book.dto.request.BookUpdateRequest;
import com.lms.library.book.dto.response.BookResponse;
import com.lms.library.book.entity.Book;
import com.lms.library.book.exception.DuplicateResourceException;
import com.lms.library.book.exception.ResourceNotFoundException;
import com.lms.library.book.dto.mapper.BookMapper;
import com.lms.library.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j // dùng để log (debug, tracking production)
@Service
@RequiredArgsConstructor // tự inject dependency qua constructor
public class BookService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    // ================= CREATE =================
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {

        // Check trùng ISBN (business rule)
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Sách với mã ISBN [" + request.getIsbn() + "] đã tồn tại!"
            );
        }

        // Map từ DTO -> Entity
        Book newBook = bookMapper.toEntity(request);

        // Lưu xuống DB
        Book savedBook = bookRepository.save(newBook);

        // Map ngược lại -> Response DTO
        return bookMapper.toResponse(savedBook);
    }

    // ================= GET ALL (KHÔNG FILTER) =================
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {

        log.info("Lấy toàn bộ danh sách sách (pagination)");

        // Dùng findAll chuẩn (không hack search)
        return bookRepository.findAll(pageable)
                .map(bookMapper::toResponse);
    }

    // ================= SEARCH + FILTER =================
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(
            String title,
            String author,
            String category,
            String status,   // nhận String để tránh crash enum
            Pageable pageable
    ) {

        log.info("Search sách - title: {}, author: {}, category: {}, status: {}",
                title, author, category, status);

        // ================= FIX QUAN TRỌNG =================
        // Convert String → Enum AN TOÀN (KHÔNG crash)
        Book.BookStatus statusEnum = null;

        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Book.BookStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Status không hợp lệ. Chỉ chấp nhận: AVAILABLE, OUT_OF_STOCK, ARCHIVED"
                );
            }
        }

        // Gọi repository (query động)
        return bookRepository
                .searchBooks(title, author, category, statusEnum, pageable)
                .map(bookMapper::toResponse);
    }

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {

        log.info("Đang tìm sách với ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy sách với ID: {}", id);
                    return new ResourceNotFoundException(
                            "Không tìm thấy sách với ID: " + id
                    );
                });

        return bookMapper.toResponse(book);
    }

    // ================= UPDATE =================
    @Transactional
    public BookResponse updateBook(UUID id, BookUpdateRequest request) {

        log.info("Đang cập nhật sách ID: {}", id);

        // Tìm sách
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy sách với ID: " + id)
                );

        // Map field update (MapStruct xử lý)
        bookMapper.updateEntityFromRequest(request, existingBook);

        // Lưu lại DB
        Book savedBook = bookRepository.save(existingBook);

        return bookMapper.toResponse(savedBook);
    }

    // ================= DELETE (SOFT DELETE) =================
    @Transactional
    public void deleteBook(UUID id) {

        log.info("Đang xóa mềm sách ID: {}", id);

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy sách với ID: " + id)
                );

        // ❗ KHÔNG xóa DB -> chỉ đổi trạng thái
        existingBook.setStatus(Book.BookStatus.ARCHIVED);

        bookRepository.save(existingBook);

        log.info("Sách {} đã chuyển sang ARCHIVED", id);
    }

    @Transactional
    public void borrowBook(UUID id) {

        log.info("Borrow sách ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        // ❗ CHECK QUAN TRỌNG NHẤT
        if (book.getAvailableQty() <= 0) {
            throw new IllegalStateException("Sách đã hết, không thể mượn");
        }

        // Giảm số lượng
        book.setAvailableQty(book.getAvailableQty() - 1);

        // Update status
        if (book.getAvailableQty() == 0) {
            book.setStatus(Book.BookStatus.OUT_OF_STOCK);
        }

        bookRepository.save(book);
    }

    @Transactional
    public void returnBook(UUID id) {

        log.info("Return sách ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        // Tăng số lượng
        book.setAvailableQty(book.getAvailableQty() + 1);

        // Update status
        book.setStatus(Book.BookStatus.AVAILABLE);

        bookRepository.save(book);
    }
}