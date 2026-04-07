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

    // Explicit getters
    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public UUID getBookId() { return bookId; }
    public UUID getBookCopyId() { return bookCopyId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public ZonedDateTime getBorrowTime() { return borrowTime; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public ZonedDateTime getReturnTime() { return returnTime; }
    public Integer getExtensionCount() { return extensionCount; }
    public Integer getMaxExtensions() { return maxExtensions; }
    public LocalDate getLastExtensionDate() { return lastExtensionDate; }
    public BorrowStatus getBorrowStatus() { return borrowStatus; }
    public BookCondition getConditionOnBorrow() { return conditionOnBorrow; }
    public BookCondition getConditionOnReturn() { return conditionOnReturn; }
    public String getNotes() { return notes; }
    public String getReturnNotes() { return returnNotes; }

    // Explicit setters
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public void setBookCopyId(UUID bookCopyId) { this.bookCopyId = bookCopyId; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public void setBorrowTime(ZonedDateTime borrowTime) { this.borrowTime = borrowTime; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setReturnTime(ZonedDateTime returnTime) { this.returnTime = returnTime; }
    public void setExtensionCount(Integer extensionCount) { this.extensionCount = extensionCount; }
    public void setMaxExtensions(Integer maxExtensions) { this.maxExtensions = maxExtensions; }
    public void setLastExtensionDate(LocalDate lastExtensionDate) { this.lastExtensionDate = lastExtensionDate; }
    public void setBorrowStatus(BorrowStatus borrowStatus) { this.borrowStatus = borrowStatus; }
    public void setConditionOnBorrow(BookCondition conditionOnBorrow) { this.conditionOnBorrow = conditionOnBorrow; }
    public void setConditionOnReturn(BookCondition conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setReturnNotes(String returnNotes) { this.returnNotes = returnNotes; }

    public static BorrowRecordBuilder builder() {
        return new BorrowRecordBuilder();
    }

    public static class BorrowRecordBuilder {
        private UUID id;
        private UUID memberId;
        private UUID bookId;
        private UUID bookCopyId;
        private LocalDate borrowDate = LocalDate.now();
        private ZonedDateTime borrowTime = ZonedDateTime.now();
        private LocalDate dueDate;
        private LocalDate returnDate;
        private ZonedDateTime returnTime;
        private Integer extensionCount = 0;
        private Integer maxExtensions = 3;
        private LocalDate lastExtensionDate;
        private BorrowStatus borrowStatus = BorrowStatus.ACTIVE;
        private BookCondition conditionOnBorrow;
        private BookCondition conditionOnReturn;
        private String notes;
        private String returnNotes;

        public BorrowRecordBuilder id(UUID id) { this.id = id; return this; }
        public BorrowRecordBuilder memberId(UUID memberId) { this.memberId = memberId; return this; }
        public BorrowRecordBuilder bookId(UUID bookId) { this.bookId = bookId; return this; }
        public BorrowRecordBuilder bookCopyId(UUID bookCopyId) { this.bookCopyId = bookCopyId; return this; }
        public BorrowRecordBuilder borrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; return this; }
        public BorrowRecordBuilder borrowTime(ZonedDateTime borrowTime) { this.borrowTime = borrowTime; return this; }
        public BorrowRecordBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public BorrowRecordBuilder returnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public BorrowRecordBuilder returnTime(ZonedDateTime returnTime) { this.returnTime = returnTime; return this; }
        public BorrowRecordBuilder extensionCount(Integer extensionCount) { this.extensionCount = extensionCount; return this; }
        public BorrowRecordBuilder maxExtensions(Integer maxExtensions) { this.maxExtensions = maxExtensions; return this; }
        public BorrowRecordBuilder lastExtensionDate(LocalDate lastExtensionDate) { this.lastExtensionDate = lastExtensionDate; return this; }
        public BorrowRecordBuilder borrowStatus(BorrowStatus borrowStatus) { this.borrowStatus = borrowStatus; return this; }
        public BorrowRecordBuilder conditionOnBorrow(BookCondition conditionOnBorrow) { this.conditionOnBorrow = conditionOnBorrow; return this; }
        public BorrowRecordBuilder conditionOnReturn(BookCondition conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; return this; }
        public BorrowRecordBuilder notes(String notes) { this.notes = notes; return this; }
        public BorrowRecordBuilder returnNotes(String returnNotes) { this.returnNotes = returnNotes; return this; }

        public BorrowRecord build() {
            BorrowRecord record = new BorrowRecord();
            record.id = this.id;
            record.memberId = this.memberId;
            record.bookId = this.bookId;
            record.bookCopyId = this.bookCopyId;
            record.borrowDate = this.borrowDate;
            record.borrowTime = this.borrowTime;
            record.dueDate = this.dueDate;
            record.returnDate = this.returnDate;
            record.returnTime = this.returnTime;
            record.extensionCount = this.extensionCount;
            record.maxExtensions = this.maxExtensions;
            record.lastExtensionDate = this.lastExtensionDate;
            record.borrowStatus = this.borrowStatus;
            record.conditionOnBorrow = this.conditionOnBorrow;
            record.conditionOnReturn = this.conditionOnReturn;
            record.notes = this.notes;
            record.returnNotes = this.returnNotes;
            return record;
        }
    }
}
