package com.lms.library.application.service;

import com.lms.library.application.dto.BookResponse;
import com.lms.library.domain.entity.Book;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.spec.BookSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// Äáº·t readOnly true sáº½ giÃºp Hibernate khÃ´ng táº¡o Dirty Checking -> TÄƒng Ä‘Ã¡ng ká»ƒ performance khi Get data
@Transactional(readOnly = true)
public class BookSearchService {

    private final BookRepository bookRepository;

    /**
     * API Full Search (TÃ¬m kiáº¿m káº¿t há»£p & Filter & Sort & Pagination)
     * Thá»a mÃ£n yÃªu cáº§u: YÃªu cáº§u phÃ¢n trang, Sort, tÃ¬m káº¿t há»£p title/author/isbn (q), Lá»c category
     * Fix 2.1: Cache params ngáº·t nghÃ¨o hÆ¡n (+ pageSize + sort).
     * Fix 2.2: sync = true chá»‘ng cache stampede.
     */
    @Cacheable(value = "searchBooks", key = "{#keyword, #isbn, #category, #statusStr, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}", sync = true)
    public Page<BookResponse> advancedSearch(String keyword, String isbn, String category, String statusStr, Pageable pageable) {
        log.info("Advanced Searching: q={}, isbn={}, category={}, status={}", keyword, isbn, category, statusStr);
        Book.BookStatus status = parseStatus(statusStr);
        
        // Strategy: Máº·c Ä‘á»‹nh náº¿u Client khÃ´ng truyá»n Criterias Sort, ta Fallback sáº¯p xáº¿p theo Title ASC
        Pageable finalPageable = pageable.getSort().isUnsorted() 
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title").ascending())
                : pageable;

        // Build CÃ¢u Query Ä‘á»™ng tá»« cÃ¡c tham sá»‘ Search
        Specification<Book> spec = BookSpecification.search(keyword, isbn, category, status);
        
        // Execute & Map Result
        return bookRepository.findAll(spec, finalPageable).map(BookResponse::from);
    }

    /**
     * API Lightweight: DÃ nh cho UI Search Autocomplete Dropsdown.
     * Tráº£ vá» nhanh tá»‘i Ä‘a 5 items khá»›p vá»›i text Ä‘ang nháº­p.
     * Fix 2.3: Giá»›i háº¡n record size cá»§a cache, trÃ¡nh spam mÃ  memory leak (tá»« khÃ³a pháº£i > 2 má»›i cache)
     */
    @Cacheable(value = "autocompleteBooks", key = "#keyword", unless = "#keyword.length() < 2 || #result == null || #result.isEmpty()")
    public List<BookResponse> autocomplete(String keyword) {
        // Chá»‰ trigger search Dropdown náº¿u User gÃµ >= 2 kÃ½ tá»± (Giáº£m táº£i Load cho DB)
        if (keyword == null || keyword.trim().length() < 2) return List.of(); 
        
        log.info("Autocomplete Suggesting for: {}", keyword);
        Specification<Book> spec = BookSpecification.autocomplete(keyword);
        
        // Láº¥y 5 dÃ²ng Ä‘áº§u tiÃªn (Top 5 Result) vá»›i Prefix LIKE Match
        Pageable top5 = PageRequest.of(0, 5, Sort.by("title").ascending());
        
        return bookRepository.findAll(spec, top5)
                .stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    private Book.BookStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) return null;
        try {
            return Book.BookStatus.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Lá»—i sai Enum Status (Bá» qua lá»c status): {}", statusStr);
            return null; // Bá» qua lá»c status sai Ä‘á»‹nh dáº¡ng
        }
    }
}

