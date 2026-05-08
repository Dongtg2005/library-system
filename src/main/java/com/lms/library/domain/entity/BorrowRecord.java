package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "borrow_records")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "book_copy_id")
    private UUID bookCopyId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    private LocalDate borrowDate;

    @Column(name = "borrow_time")
    private ZonedDateTime borrowTime;

    private LocalDate dueDate;

    private LocalDate returnDate;

    @Column(name = "return_time")
    private ZonedDateTime returnTime;

    @Column(name = "extension_count")
    private Integer extensionCount = 0;

    @Column(name = "max_extensions")
    private Integer maxExtensions = 2;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    @Column(name = "max_renewals")
    private Integer maxRenewals = 3;

    @Column(name = "last_extension_date")
    private LocalDate lastExtensionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrow_status", length = 20)
    private BorrowStatus borrowStatus = BorrowStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_borrow", length = 20)
    private BookCondition conditionOnBorrow;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_return", length = 20)
    private BookCondition conditionOnReturn;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "fine_paid")
    private Boolean finePaid = false;

    @Column(name = "fine_paid_at")
    private LocalDateTime finePaidAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "librarian_id")
    private Long librarianId;

    @Column(name = "return_librarian_id")
    private Long returnLibrarianId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (extensionCount == null) extensionCount = 0;
        if (maxExtensions == null) maxExtensions = 2;
        if (renewalCount == null) renewalCount = 0;
        if (maxRenewals == null) maxRenewals = 3;
        if (borrowStatus == null) borrowStatus = BorrowStatus.ACTIVE;
        if (fineAmount == null) fineAmount = BigDecimal.ZERO;
        if (finePaid == null) finePaid = false;
    }

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
        if (dueDate == null) {
            return 0;
        }
        if (isReturned() && returnDate != null) {
            if (returnDate.isAfter(dueDate)) {
                return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
            }
            return 0;
        }
        if (!isOverdue()) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public boolean canExtend() {
        return isActive() &&
               extensionCount < maxExtensions &&
               !isOverdue();
    }

    public boolean canRenew() {
        return isActive() &&
               renewalCount < maxRenewals &&
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

    public void renewLoan(int additionalDays) {
        if (!canRenew()) {
            throw new IllegalStateException("Cannot renew loan for this borrow record");
        }
        this.renewalCount++;
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

    public boolean hasFine() {
        return fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isFinePaid() {
        return Boolean.TRUE.equals(finePaid);
    }

    public void payFine() {
        this.finePaid = true;
        this.finePaidAt = LocalDateTime.now();
    }
}
