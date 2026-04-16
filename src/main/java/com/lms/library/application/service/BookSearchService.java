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
// Setting readOnly=true prevents Hibernate from running Dirty Checking -> significantly boosts read performance
@Transactional(readOnly = true)
public class BookSearchService {

    private final BookRepository bookRepository;

    /**
     * Full Search API: keyword + filter + sort + pagination combined.
     * Cache key includes pageSize and sort to avoid stale results.
     * sync=true prevents cache stampede on high concurrency.
     */
    @Cacheable(
        value = "searchBooks",
        key = "{#keyword, #isbn, #category, #statusStr, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}",
        sync = true
    )
    public Page<BookResponse> advancedSearch(String keyword, String isbn, String category, String statusStr, Pageable pageable) {
        log.info("Advanced Searching: q={}, isbn={}, category={}, status={}", keyword, isbn, category, statusStr);
        Book.BookStatus status = parseStatus(statusStr);

        // Default sort by title ASC if client does not provide sort criteria
        Pageable finalPageable = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title").ascending())
                : pageable;

        // Build dynamic query from search parameters
        Specification<Book> spec = BookSpecification.search(keyword, isbn, category, status);

        // Execute and map results
        return bookRepository.findAll(spec, finalPageable).map(BookResponse::from);
    }

    /**
     * Lightweight API for UI Search Autocomplete Dropdown.
     * Returns at most 5 matching items based on the current input.
     * Cache is skipped for keywords shorter than 2 characters to prevent memory bloat.
     */
    @Cacheable(
        value = "autocompleteBooks",
        key = "#keyword",
        unless = "#keyword.length() < 2 || #result == null || #result.isEmpty()"
    )
    public List<BookResponse> autocomplete(String keyword) {
        // Only trigger search dropdown if user has typed >= 2 characters (reduces DB load)
        if (keyword == null || keyword.trim().length() < 2) return List.of();

        log.info("Autocomplete suggesting for: {}", keyword);
        Specification<Book> spec = BookSpecification.autocomplete(keyword);

        // Top 5 results with prefix LIKE match
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
            log.warn("Invalid status value (ignoring status filter): {}", statusStr);
            return null;
        }
    }
}
