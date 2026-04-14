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
     * Dá»±ng cÃ¢u truy váº¥n Ä‘á»™ng (Dynamic Query) cho tÃ­nh nÄƒng Advanced Search
     */
    public static Specification<Book> search(String keyword, String isbn, String category, Book.BookStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tá»‘i Æ°u N+1 Query: Náº¿u khÃ´ng pháº£i lÃ  query Ä‘áº¿m sá»‘ lÆ°á»£ng (count) thÃ¬ fetch JOIN báº£ng categories
            if (Long.class != query.getResultType()) {
                root.fetch("categories", JoinType.LEFT);
            }
            
            // Xá» lÃ½ lá»—i phÃ¢n trang sai do trÃ¹ng lÄƒp data khi JOIN trÃªn báº£ng OneToMany/ManyToMany
            query.distinct(true);

            // 1. TÃ¬m theo tá»« khÃ³a (Partial Match trÃªn Title vÃ  Author)
            if (keyword != null && !keyword.isBlank()) {
                // Tráº£ vá» láº¡i %keyword% Ä‘á»ƒ giá»¯ tráº£i nghiá»‡m tÃ¬m kiáº¿m infix cho Advanced Search
                // Trong tÆ°Æ¡ng lai cÃ³ thá»ƒ tÃch há»£p PostgreSQL GIN index Ä‘á»ƒ boost pháº§n nÃ y
                String likePattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate authorMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, authorMatch));
            }

            // 2. TÃ¬m chÃ­nh xÃ¡c theo ISBN
            if (isbn != null && !isbn.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("isbn"), isbn.trim()));
            }

            // 3. Lá»c theo tráº¡ng thÃ¡i sÃ¡ch (Available / Out of stock...)
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 4. Lá»c theo thá»ƒ loáº¡i (Giáº£i quyáº¿t bÃ i toÃ¡n Mismatch - Báº¯t buá»™c join báº£ng categories)
            if (category != null && !category.isBlank()) {
                Join<Book, Category> categoryJoin = root.join("categories");
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(categoryJoin.get("name")), category.toLowerCase().trim()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Dá»±ng cÃ¢u truy váº¥n cá»±c láº¹ cho tÃ­nh nÄƒng search gá»£i Ã½ (Autocomplete)
     * Æ¯u tiÃªn Prefix Match (VÃ­ dá»¥ gÃµ 'Har' ra 'Harry Potter') giÃºp táº­n dá»¥ng sá»©c máº¡nh cá»§a DB Index
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

