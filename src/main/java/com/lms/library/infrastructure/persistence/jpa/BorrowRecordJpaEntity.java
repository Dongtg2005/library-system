package com.lms.library.infrastructure.persistence.jpa;

import com.lms.library.domain.entity.BorrowRecord;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecordJpaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "book_copy_id")
    private UUID bookCopyId;

    @Column(name = "borrow_date", nullable = false)
    @Builder.Default
    private LocalDate borrowDate = LocalDate.now();

    @Column(name = "borrow_time", nullable = false)
    @Builder.Default
    private ZonedDateTime borrowTime = ZonedDateTime.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "return_time")
    private ZonedDateTime returnTime;

    @Column(name = "extension_count")
    @Builder.Default
    private Integer extensionCount = 0;

    @Column(name = "max_extensions")
    @Builder.Default
    private Integer maxExtensions = 3;

    @Column(name = "last_extension_date")
    private LocalDate lastExtensionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrow_status", nullable = false, length = 50)
    @Builder.Default
    private BorrowRecord.BorrowStatus borrowStatus = BorrowRecord.BorrowStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_borrow", length = 50)
    private BorrowRecord.BookCondition conditionOnBorrow;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_return", length = 50)
    private BorrowRecord.BookCondition conditionOnReturn;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;
    
    public BorrowRecord toDomainModel() {
        return BorrowRecord.builder()
                .id(id)
                .memberId(memberId)
                .bookId(bookId)
                .bookCopyId(bookCopyId)
                .borrowDate(borrowDate)
                .borrowTime(borrowTime)
                .dueDate(dueDate)
                .returnDate(returnDate)
                .returnTime(returnTime)
                .extensionCount(extensionCount)
                .maxExtensions(maxExtensions)
                .lastExtensionDate(lastExtensionDate)
                .borrowStatus(borrowStatus)
                .conditionOnBorrow(conditionOnBorrow)
                .conditionOnReturn(conditionOnReturn)
                .notes(notes)
                .returnNotes(returnNotes)
                .build();
    }
    
    public static BorrowRecordJpaEntity fromDomainModel(BorrowRecord borrowRecord) {
        return BorrowRecordJpaEntity.builder()
                .id(borrowRecord.getId())
                .memberId(borrowRecord.getMemberId())
                .bookId(borrowRecord.getBookId())
                .bookCopyId(borrowRecord.getBookCopyId())
                .borrowDate(borrowRecord.getBorrowDate())
                .borrowTime(borrowRecord.getBorrowTime())
                .dueDate(borrowRecord.getDueDate())
                .returnDate(borrowRecord.getReturnDate())
                .returnTime(borrowRecord.getReturnTime())
                .extensionCount(borrowRecord.getExtensionCount())
                .maxExtensions(borrowRecord.getMaxExtensions())
                .lastExtensionDate(borrowRecord.getLastExtensionDate())
                .borrowStatus(borrowRecord.getBorrowStatus())
                .conditionOnBorrow(borrowRecord.getConditionOnBorrow())
                .conditionOnReturn(borrowRecord.getConditionOnReturn())
                .notes(borrowRecord.getNotes())
                .returnNotes(borrowRecord.getReturnNotes())
                .build();
    }
}
