package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {
    
    private UUID id;
    private UUID borrowRecordId;
    private UUID memberId;
    private BigDecimal amount;
    private FineType fineType;
    private FineStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    
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
