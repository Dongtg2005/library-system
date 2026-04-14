package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "books", indexes = {
    // Đánh index để Query search (ISBN, Title, Author) chạy cực nhanh
    @Index(name = "idx_book_isbn", columnList = "isbn", unique = true),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_author", columnList = "author")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 255)
    private String publisher;

    private LocalDate publicationDate;

    @Column(length = 10)
    private String language = "vi";

    private Integer pages;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookFormat format = BookFormat.PHYSICAL;

    @Column(length = 500)
    private String fileUrl;

    private Long fileSize;

    @Column(length = 500)
    private String coverImageUrl;

    @Column(nullable = false)
    private Integer totalQuantity = 1;

    @Column(nullable = false)
    private Integer availableQty = 1;

    private Integer borrowedQuantity = 0;

    private Integer reservedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    private Integer ratingCount = 0;

    private Integer viewCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_tags",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (availableQty == null) availableQty = totalQuantity;
        if (language == null) language = "vi";
        if (format == null) format = BookFormat.PHYSICAL;
        if (status == null) status = BookStatus.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BookStatus {
        AVAILABLE, OUT_OF_STOCK, ARCHIVED, DAMAGED
    }

    public enum BookFormat {
        PHYSICAL, EBOOK, AUDIOBOOK
    }

    public boolean isAvailable() {
        return BookStatus.AVAILABLE.equals(this.status) && availableQty > 0;
    }

    public boolean isOutOfStock() {
        return availableQty <= 0;
    }

    public boolean isEbook() {
        return BookFormat.EBOOK.equals(this.format);
    }

    public boolean isAudiobook() {
        return BookFormat.AUDIOBOOK.equals(this.format);
    }

    public void borrowBook() {
        if (isOutOfStock()) {
            throw new IllegalStateException("Book is out of stock and cannot be borrowed");
        }
        this.availableQty--;
        if (this.borrowedQuantity == null) this.borrowedQuantity = 0;
        this.borrowedQuantity++;
        if (this.availableQty <= 0) {
            this.status = BookStatus.OUT_OF_STOCK;
        }
    }

    public void returnBook() {
        this.availableQty++;
        if (this.borrowedQuantity != null && this.borrowedQuantity > 0) {
            this.borrowedQuantity--;
        }
        if (this.availableQty > 0 && BookStatus.OUT_OF_STOCK.equals(this.status)) {
            this.status = BookStatus.AVAILABLE;
        }
    }

    public void archive() {
        this.status = BookStatus.ARCHIVED;
    }

    public void incrementViewCount() {
        if (this.viewCount == null) this.viewCount = 0;
        this.viewCount++;
    }

    public void addRating(int newRating) {
        if (this.ratingCount == null) this.ratingCount = 0;
        if (this.averageRating == null) this.averageRating = BigDecimal.ZERO;

        BigDecimal totalRating = this.averageRating.multiply(BigDecimal.valueOf(this.ratingCount));
        this.ratingCount++;
        this.averageRating = totalRating.add(BigDecimal.valueOf(newRating))
                .divide(BigDecimal.valueOf(this.ratingCount), 2, BigDecimal.ROUND_HALF_UP);
    }
}
