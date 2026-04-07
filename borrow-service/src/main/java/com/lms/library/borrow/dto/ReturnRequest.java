package com.lms.library.borrow.dto;

import com.lms.library.borrow.entity.enums.BookCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for processing book return")
public class ReturnRequest {
    
    @NotNull(message = "Borrow record ID is required")
    @Schema(description = "ID of the borrow record to return", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID borrowRecordId;
    
    @Schema(description = "Condition of the book when returned", example = "GOOD")
    private BookCondition conditionOnReturn;
    
    @Schema(description = "Notes about the book condition or return process", example = "Book has minor highlighting on page 15")
    private String returnNotes;
}
