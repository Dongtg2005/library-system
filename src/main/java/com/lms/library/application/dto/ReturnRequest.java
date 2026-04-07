package com.lms.library.application.dto;

import com.lms.library.domain.entity.BorrowRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReturnRequest {
    
    @NotNull(message = "Borrow record ID is required")
    private UUID borrowRecordId;
    
    @NotNull(message = "Condition on return is required")
    private BorrowRecord.BookCondition conditionOnReturn;
    
    private String returnNotes;
}
