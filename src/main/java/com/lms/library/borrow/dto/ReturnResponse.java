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

    public UUID getBorrowRecordId() { return borrowRecordId; }
    public void setBorrowRecordId(UUID borrowRecordId) { this.borrowRecordId = borrowRecordId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public ZonedDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(ZonedDateTime returnTime) { this.returnTime = returnTime; }
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }
    public BookCondition getConditionOnReturn() { return conditionOnReturn; }
    public void setConditionOnReturn(BookCondition conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; }
    public Integer getOverdueDays() { return overdueDays; }
    public void setOverdueDays(Integer overdueDays) { this.overdueDays = overdueDays; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }
    public Boolean getProcessedSuccessfully() { return processedSuccessfully; }
    public void setProcessedSuccessfully(Boolean processedSuccessfully) { this.processedSuccessfully = processedSuccessfully; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static ReturnResponseBuilder builder() { return new ReturnResponseBuilder(); }

    public static class ReturnResponseBuilder {
        private UUID borrowRecordId;
        private UUID memberId;
        private UUID bookId;
        private LocalDate borrowDate;
        private LocalDate dueDate;
        private LocalDate returnDate;
        private ZonedDateTime returnTime;
        private BorrowStatus status;
        private BookCondition conditionOnReturn;
        private Integer overdueDays;
        private BigDecimal fineAmount;
        private Boolean processedSuccessfully;
        private String message;

        public ReturnResponseBuilder borrowRecordId(UUID borrowRecordId) { this.borrowRecordId = borrowRecordId; return this; }
        public ReturnResponseBuilder memberId(UUID memberId) { this.memberId = memberId; return this; }
        public ReturnResponseBuilder bookId(UUID bookId) { this.bookId = bookId; return this; }
        public ReturnResponseBuilder borrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; return this; }
        public ReturnResponseBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public ReturnResponseBuilder returnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public ReturnResponseBuilder returnTime(ZonedDateTime returnTime) { this.returnTime = returnTime; return this; }
        public ReturnResponseBuilder status(BorrowStatus status) { this.status = status; return this; }
        public ReturnResponseBuilder conditionOnReturn(BookCondition conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; return this; }
        public ReturnResponseBuilder overdueDays(Integer overdueDays) { this.overdueDays = overdueDays; return this; }
        public ReturnResponseBuilder fineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; return this; }
        public ReturnResponseBuilder processedSuccessfully(Boolean processedSuccessfully) { this.processedSuccessfully = processedSuccessfully; return this; }
        public ReturnResponseBuilder message(String message) { this.message = message; return this; }

        public ReturnResponse build() {
            ReturnResponse response = new ReturnResponse();
            response.borrowRecordId = this.borrowRecordId;
            response.memberId = this.memberId;
            response.bookId = this.bookId;
            response.borrowDate = this.borrowDate;
            response.dueDate = this.dueDate;
            response.returnDate = this.returnDate;
            response.returnTime = this.returnTime;
            response.status = this.status;
            response.conditionOnReturn = this.conditionOnReturn;
            response.overdueDays = this.overdueDays;
            response.fineAmount = this.fineAmount;
            response.processedSuccessfully = this.processedSuccessfully;
            response.message = this.message;
            return response;
        }
    }
}
