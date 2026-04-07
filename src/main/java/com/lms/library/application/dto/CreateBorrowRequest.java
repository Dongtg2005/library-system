package com.lms.library.application.dto;

import com.lms.library.domain.entity.BorrowRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBorrowRequest {
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
    
    private BorrowRecord.BookCondition conditionOnBorrow;
    
    private String notes;
}
