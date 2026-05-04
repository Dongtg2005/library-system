package com.lms.library.application.dto;

import com.lms.library.domain.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private UUID id;
    private String isbn;
    private String title;
    private String author;
    private String category; // Kept for backwards compatibility
    private java.util.List<CategoryResponse> categories;
    private Integer totalQuantity;
    private Integer availableQty;
    private Book.BookStatus status;
    private String coverImageUrl;
    private java.math.BigDecimal averageRating;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BookResponse from(Book book) {
        // Get first category name if available
        String categoryName = null;
        java.util.List<CategoryResponse> catResponses = null;
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            categoryName = book.getCategories().stream().map(c -> c.getName()).collect(java.util.stream.Collectors.joining(", "));
            catResponses = book.getCategories().stream().map(CategoryResponse::from).toList();
        }
        
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(categoryName)
                .categories(catResponses)
                .coverImageUrl(book.getCoverImageUrl())
                .totalQuantity(book.getTotalQuantity())
                .availableQty(book.getAvailableQty())
                .status(book.getStatus())
                .averageRating(book.getAverageRating())
                .ratingCount(book.getRatingCount())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
