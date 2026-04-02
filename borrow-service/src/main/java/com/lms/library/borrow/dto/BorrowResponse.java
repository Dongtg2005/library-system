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
}
