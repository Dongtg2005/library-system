package com.lms.library.domain.entity;

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
public class BorrowPolicy {
    
    private UUID id;
    private String name;
    private MemberType memberType;
    private Integer maxBooksAllowed;
    private Integer loanPeriodDays;
    private Integer maxExtensions;
    private BigDecimal finePerDay;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
    
    public enum MemberType {
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
        return finePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }
}
