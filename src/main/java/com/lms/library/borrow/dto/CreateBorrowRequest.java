package com.lms.library.borrow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBorrowRequest {

    @NotNull(message = "Book ID is required")
    private UUID bookId;

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public static CreateBorrowRequestBuilder builder() {
        return new CreateBorrowRequestBuilder();
    }

    public static class CreateBorrowRequestBuilder {
        private UUID bookId;

        public CreateBorrowRequestBuilder bookId(UUID bookId) {
            this.bookId = bookId;
            return this;
        }

        public CreateBorrowRequest build() {
            CreateBorrowRequest request = new CreateBorrowRequest();
            request.bookId = this.bookId;
            return request;
        }
    }
}
