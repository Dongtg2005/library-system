package com.lms.library.domain.entity;

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
public class BorrowRecord {
    
    private UUID id;
    private UUID memberId;
    private UUID bookId;
    private UUID bookCopyId;
    private LocalDate borrowDate;
    private ZonedDateTime borrowTime;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private ZonedDateTime returnTime;
    private Integer extensionCount;
    private Integer maxExtensions;
    private LocalDate lastExtensionDate;
    private BorrowStatus borrowStatus;
    private BookCondition conditionOnBorrow;
    private BookCondition conditionOnReturn;
    private String notes;
    private String returnNotes;
    
    public enum BorrowStatus {
        ACTIVE,
        RETURNED,
        OVERDUE,
        PENDING_APPROVAL,
        CANCELLED
    }
    
    public enum BookCondition {
        EXCELLENT,
        GOOD,
        FAIR,
        POOR,
        DAMAGED
    }
    
    public boolean isActive() {
        return BorrowStatus.ACTIVE.equals(this.borrowStatus) || 
               BorrowStatus.OVERDUE.equals(this.borrowStatus);
    }
    
    public boolean isReturned() {
        return BorrowStatus.RETURNED.equals(this.borrowStatus);
    }
    
    public boolean isOverdue() {
        return BorrowStatus.OVERDUE.equals(this.borrowStatus) || 
               (isActive() && dueDate != null && LocalDate.now().isAfter(dueDate));
    }
    
    public int getOverdueDays() {
        if (!isOverdue() || dueDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
    
    public boolean canExtend() {
        return isActive() && 
               extensionCount < maxExtensions && 
               !isOverdue();
    }
    
    public void extendLoan(int additionalDays) {
        if (!canExtend()) {
            throw new IllegalStateException("Cannot extend loan for this borrow record");
        }
        this.extensionCount++;
        this.lastExtensionDate = LocalDate.now();
        if (this.dueDate != null) {
            this.dueDate = this.dueDate.plusDays(additionalDays);
        }
    }
    
    public void returnBook(BookCondition returnCondition) {
        this.returnDate = LocalDate.now();
        this.returnTime = ZonedDateTime.now();
        this.conditionOnReturn = returnCondition;
        this.borrowStatus = BorrowStatus.RETURNED;
    }
    
    public void markAsOverdue() {
        if (isActive()) {
            this.borrowStatus = BorrowStatus.OVERDUE;
        }
    }
}
