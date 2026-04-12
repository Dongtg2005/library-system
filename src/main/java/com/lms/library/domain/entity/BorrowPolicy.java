package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "borrow_policies")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", length = 20, nullable = false)
    private MemberType memberType;

    @Column(name = "max_books_allowed")
    private Integer maxBooksAllowed = 5;

    @Column(name = "loan_period_days")
    private Integer loanPeriodDays = 14;

    @Column(name = "max_extensions")
    private Integer maxExtensions = 2;

    @Column(name = "fine_per_day", precision = 10, scale = 2)
    private BigDecimal finePerDay = new BigDecimal("1000.00");

    @Column(name = "max_fine", precision = 10, scale = 2)
    private BigDecimal maxFine;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays = 0;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (maxBooksAllowed == null) maxBooksAllowed = 5;
        if (loanPeriodDays == null) loanPeriodDays = 14;
        if (maxExtensions == null) maxExtensions = 2;
        if (finePerDay == null) finePerDay = new BigDecimal("1000.00");
        if (gracePeriodDays == null) gracePeriodDays = 0;
        if (isActive == null) isActive = true;
    }

    public enum MemberType {
        GUEST,
        USER,
        LIBRARIAN,
        ADMIN,
        STUDENT,
        FACULTY,
        STAFF,
        EXTERNAL
    }

    public boolean isEffective() {
        LocalDate now = LocalDate.now();
        return Boolean.TRUE.equals(isActive) &&
               (effectiveFrom == null || !now.isBefore(effectiveFrom)) &&
               (effectiveTo == null || !now.isAfter(effectiveTo));
    }

    public boolean canBorrowMoreBooks(int currentBorrowedBooks) {
        return isEffective() && currentBorrowedBooks < maxBooksAllowed;
    }

    public BigDecimal calculateOverdueFine(int overdueDays) {
        if (finePerDay == null || overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        // Apply grace period
        int actualOverdueDays = Math.max(0, overdueDays - (gracePeriodDays != null ? gracePeriodDays : 0));
        if (actualOverdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal calculatedFine = finePerDay.multiply(BigDecimal.valueOf(actualOverdueDays));

        // Cap at max fine if set
        if (maxFine != null && calculatedFine.compareTo(maxFine) > 0) {
            return maxFine;
        }

        return calculatedFine;
    }

    public boolean isGracePeriodActive(int overdueDays) {
        return overdueDays <= (gracePeriodDays != null ? gracePeriodDays : 0);
    }
}
