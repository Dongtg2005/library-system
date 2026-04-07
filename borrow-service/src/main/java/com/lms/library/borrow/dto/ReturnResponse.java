package com.lms.library.borrow.dto;

import com.lms.library.borrow.entity.enums.BookCondition;
import com.lms.library.borrow.entity.enums.BorrowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing return processing results")
public class ReturnResponse {
    
    @Schema(description = "ID of the borrow record", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID borrowRecordId;
    
    @Schema(description = "ID of the member who borrowed the book", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID memberId;
    
    @Schema(description = "ID of the returned book", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID bookId;
    
    @Schema(description = "Original borrow date", example = "2024-01-01")
    private LocalDate borrowDate;
    
    @Schema(description = "Original due date", example = "2024-01-15")
    private LocalDate dueDate;
    
    @Schema(description = "Actual return date", example = "2024-01-20")
    private LocalDate returnDate;
    
    @Schema(description = "Actual return time with timezone", example = "2024-01-20T10:30:00Z")
    private ZonedDateTime returnTime;
    
    @Schema(description = "Current status of the borrow record", example = "RETURNED")
    private BorrowStatus status;
    
    @Schema(description = "Condition of the book when returned", example = "GOOD")
    private BookCondition conditionOnReturn;
    
    @Schema(description = "Number of days the return is overdue", example = "5")
    private Integer overdueDays;
    
    @Schema(description = "Calculated fine for overdue return", example = "25.50")
    private BigDecimal fineAmount;
    
    @Schema(description = "Whether the return was processed successfully", example = "true")
    private Boolean processedSuccessfully;
    
    @Schema(description = "Message about the return process", example = "Book returned successfully with 5 days overdue")
    private String message;
}
