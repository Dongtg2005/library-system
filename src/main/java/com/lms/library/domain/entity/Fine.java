package com.lms.library.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "fines")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "borrow_record_id", nullable = false)
    private UUID borrowRecordId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type", length = 20)
    private FineType fineType = FineType.OVERDUE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FineStatus status = FineStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fineType == null) fineType = FineType.OVERDUE;
        if (status == null) status = FineStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum FineType {
        OVERDUE,
        DAMAGE,
        LOSS,
        OTHER
    }
    
    public enum FineStatus {
        PENDING,
        PAID,
        WAIVED,
        CANCELLED
    }
    
    public boolean isPending() {
        return FineStatus.PENDING.equals(this.status);
    }
    
    public boolean isPaid() {
        return FineStatus.PAID.equals(this.status);
    }
    
    public boolean isOutstanding() {
        return isPending() && amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public void markAsPaid() {
        this.status = FineStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void waiveFine() {
        this.status = FineStatus.WAIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancelFine() {
        this.status = FineStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
