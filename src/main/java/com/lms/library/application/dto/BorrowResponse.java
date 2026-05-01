package com.lms.library.application.dto;

import com.lms.library.domain.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowResponse {
    private UUID id;
    private Long memberId;
    private String memberName;
    private UUID bookId;
    private LocalDate borrowDate;
    private ZonedDateTime borrowTime;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private ZonedDateTime returnTime;
    private Integer extensionCount;
    private Integer maxExtensions;
    private LocalDate lastExtensionDate;
    private BorrowRecord.BorrowStatus borrowStatus;
    private BorrowRecord.BookCondition conditionOnBorrow;
    private BorrowRecord.BookCondition conditionOnReturn;
    private String notes;
    private String returnNotes;
    private String rejectionReason;

    public static BorrowResponse from(BorrowRecord record) {
        return from(record, null);
    }

    public static BorrowResponse from(BorrowRecord record, String memberName) {
        return BorrowResponse.builder()
                .id(record.getId())
                .memberId(record.getMemberId())
                .memberName(memberName)
                .bookId(record.getBookId())
                .borrowDate(record.getBorrowDate())
                .borrowTime(record.getBorrowTime())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .returnTime(record.getReturnTime())
                .extensionCount(record.getExtensionCount())
                .maxExtensions(record.getMaxExtensions())
                .lastExtensionDate(record.getLastExtensionDate())
                .borrowStatus(record.getBorrowStatus())
                .conditionOnBorrow(record.getConditionOnBorrow())
                .conditionOnReturn(record.getConditionOnReturn())
                .notes(record.getNotes())
                .returnNotes(record.getReturnNotes())
                .rejectionReason(record.getRejectionReason())
                .build();
    }
}
