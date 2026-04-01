package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.FineStatus;
import com.lms.library.borrow.entity.enums.FineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type", nullable = false, length = 50)
    private FineType fineType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "daily_rate", precision = 8, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "max_fine_amount", precision = 10, scale = 2)
    private BigDecimal maxFineAmount;

    @Column(name = "days_overdue")
    private Integer daysOverdue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private FineStatus status = FineStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_date")
    private ZonedDateTime paymentDate;

    @Column(name = "waived_by_id")
    private UUID waivedById;

    @Column(name = "waived_date")
    private ZonedDateTime waivedDate;

    @Column(name = "waiver_reason")
    private String waiverReason;
}
