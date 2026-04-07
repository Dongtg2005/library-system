package com.lms.library.application.dto;

import com.lms.library.domain.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResponse {
    private UUID borrowRecordId;
    private LocalDate returnDate;
    private BorrowRecord.BookCondition conditionOnReturn;
    private BigDecimal overdueFine;
    private String returnNotes;
}
