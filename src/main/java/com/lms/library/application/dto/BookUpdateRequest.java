package com.lms.library.application.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookUpdateRequest {
    
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;
    
    @Size(max = 150, message = "Author must be at most 150 characters")
    private String author;
    
    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;
    
    @Positive(message = "Total quantity must be positive")
    private Integer totalQuantity;
}
