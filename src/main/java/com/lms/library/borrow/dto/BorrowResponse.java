package com.lms.library.borrow.dto;

import com.lms.library.borrow.entity.enums.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowResponse {

    private UUID recordId;
    private UUID memberId;
    private UUID bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private BorrowStatus status;

    public UUID getRecordId() { return recordId; }
    public void setRecordId(UUID recordId) { this.recordId = recordId; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }

    public static BorrowResponseBuilder builder() { return new BorrowResponseBuilder(); }

    public static class BorrowResponseBuilder {
        private UUID recordId;
        private UUID memberId;
        private UUID bookId;
        private LocalDate borrowDate;
        private LocalDate dueDate;
        private BorrowStatus status;

        public BorrowResponseBuilder recordId(UUID recordId) { this.recordId = recordId; return this; }
        public BorrowResponseBuilder memberId(UUID memberId) { this.memberId = memberId; return this; }
        public BorrowResponseBuilder bookId(UUID bookId) { this.bookId = bookId; return this; }
        public BorrowResponseBuilder borrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; return this; }
        public BorrowResponseBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public BorrowResponseBuilder status(BorrowStatus status) { this.status = status; return this; }

        public BorrowResponse build() {
            BorrowResponse response = new BorrowResponse();
            response.recordId = this.recordId;
            response.memberId = this.memberId;
            response.bookId = this.bookId;
            response.borrowDate = this.borrowDate;
            response.dueDate = this.dueDate;
            response.status = this.status;
            return response;
        }
    }
}
