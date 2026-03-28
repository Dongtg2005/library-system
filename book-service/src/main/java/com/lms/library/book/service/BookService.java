package com.lms.library.book.service;

import com.lms.library.book.dto.request.BookCreateRequest;
import com.lms.library.book.dto.response.BookResponse;
import com.lms.library.book.entity.Book;
import com.lms.library.book.exception.DuplicateResourceException;
import com.lms.library.book.dto.mapper.BookMapper;
import com.lms.library.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    
    // Gọi "anh thợ vận chuyển" ra đây để dùng
    private final BookMapper bookMapper; 

    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        // Kiểm tra xem mã vạch (ISBN) này đã có trong kho chưa
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Sách với mã ISBN [" + request.getIsbn() + "] đã tồn tại!");
        }

        // Nhờ Mapper biến đổi Request thành Book
        Book newBook = bookMapper.toEntity(request);
        
        // Lưu xuống Database
        Book savedBook = bookRepository.save(newBook);

        // Nhờ Mapper biến đổi Book vừa lưu thành Response và trả về
        return bookMapper.toResponse(savedBook);
    }

    // Lấy sách có Phân trang (Không dùng List nữa, dùng Page)
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        // Lấy 1 trang (ví dụ 10 cuốn) từ Database lên, sau đó map nó sang Response
        return bookRepository.findAll(pageable)
                .map(bookMapper::toResponse); 
    }
}