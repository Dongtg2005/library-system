package com.lms.library.domain.repository.spec;

import com.lms.library.domain.entity.Book;
import com.lms.library.domain.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    /**
     * Build dynamic query for Advanced Search feature.
     * Supports keyword (title/author), isbn, category, and status filters.
     */
    public static Specification<Book> search(String keyword, String isbn, String category, Book.BookStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Optimize N+1 Query: fetch JOIN categories only for non-count queries
            if (Long.class != query.getResultType()) {
                root.fetch("categories", JoinType.LEFT);
            }

            // Prevent duplicate rows caused by JOIN on ManyToMany/OneToMany
            query.distinct(true);

            // 1. Keyword search: partial match on title OR author
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate authorMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, authorMatch));
            }

            // 2. Exact match by ISBN
            if (isbn != null && !isbn.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("isbn"), isbn.trim()));
            }

            // 3. Filter by book status (AVAILABLE / OUT_OF_STOCK / ARCHIVED...)
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 4. Filter by category (requires join on book_categories table)
            if (category != null && !category.isBlank()) {
                Join<Book, Category> categoryJoin = root.join("categories");
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(categoryJoin.get("name")),
                    category.toLowerCase().trim()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build lightweight query for autocomplete/search suggestion.
     * Uses prefix LIKE match to leverage DB index (e.g. 'Har' -> 'Harry Potter').
     */
    public static Specification<Book> autocomplete(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String prefixPattern = keyword.toLowerCase().trim() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), prefixPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), prefixPattern)
            );
        };
    }
}
