package com.lms.library.infrastructure.persistence.jpa;

import com.lms.library.domain.entity.BorrowPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "borrow_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowPolicyJpaEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private BorrowPolicy.MemberType memberType;
    
    @Column(name = "max_books_allowed", nullable = false)
    private Integer maxBooksAllowed;
    
    @Column(name = "loan_period_days", nullable = false)
    private Integer loanPeriodDays;
    
    @Column(name = "max_extensions", nullable = false)
    private Integer maxExtensions;
    
    @Column(name = "fine_per_day", precision = 10, scale = 2)
    private BigDecimal finePerDay;
    
    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    public BorrowPolicy toDomainModel() {
        return BorrowPolicy.builder()
                .id(id)
                .name(name)
                .memberType(memberType)
                .maxBooksAllowed(maxBooksAllowed)
                .loanPeriodDays(loanPeriodDays)
                .maxExtensions(maxExtensions)
                .finePerDay(finePerDay)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .isActive(isActive)
                .build();
    }
    
    public static BorrowPolicyJpaEntity fromDomainModel(BorrowPolicy borrowPolicy) {
        return BorrowPolicyJpaEntity.builder()
                .id(borrowPolicy.getId())
                .name(borrowPolicy.getName())
                .memberType(borrowPolicy.getMemberType())
                .maxBooksAllowed(borrowPolicy.getMaxBooksAllowed())
                .loanPeriodDays(borrowPolicy.getLoanPeriodDays())
                .maxExtensions(borrowPolicy.getMaxExtensions())
                .finePerDay(borrowPolicy.getFinePerDay())
                .effectiveFrom(borrowPolicy.getEffectiveFrom())
                .effectiveTo(borrowPolicy.getEffectiveTo())
                .isActive(borrowPolicy.getIsActive())
                .build();
    }
}
