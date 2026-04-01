package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.BookCondition;
import com.lms.library.borrow.entity.enums.BorrowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrow_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord extends BaseEntity {

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
    private BorrowStatus borrowStatus = BorrowStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_borrow", length = 50)
    private BookCondition conditionOnBorrow;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_return", length = 50)
    private BookCondition conditionOnReturn;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;
}
