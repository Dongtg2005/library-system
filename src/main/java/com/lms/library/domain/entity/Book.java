package com.lms.library.domain.entity;

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
public class Book {
    
    private UUID id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private Integer totalQuantity;
    private Integer availableQty;
    private BookStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum BookStatus {
        AVAILABLE,
        OUT_OF_STOCK,
        ARCHIVED
    }
    
    public boolean isAvailable() {
        return BookStatus.AVAILABLE.equals(this.status) && availableQty > 0;
    }
    
    public boolean isOutOfStock() {
        return availableQty <= 0;
    }
    
    public void borrowBook() {
        if (isOutOfStock()) {
            throw new IllegalStateException("Book is out of stock and cannot be borrowed");
        }
        this.availableQty--;
        if (this.availableQty <= 0) {
            this.status = BookStatus.OUT_OF_STOCK;
        }
    }
    
    public void returnBook() {
        this.availableQty++;
        if (this.availableQty > 0 && BookStatus.OUT_OF_STOCK.equals(this.status)) {
            this.status = BookStatus.AVAILABLE;
        }
    }
    
    public void archive() {
        this.status = BookStatus.ARCHIVED;
    }
}
