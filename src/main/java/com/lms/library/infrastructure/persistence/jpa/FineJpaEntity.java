package com.lms.library.infrastructure.persistence.jpa;

import com.lms.library.domain.entity.Fine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineJpaEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "borrow_record_id", nullable = false)
    private UUID borrowRecordId;
    
    @Column(name = "member_id", nullable = false)
    private UUID memberId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Fine.FineType fineType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Fine.FineStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;
    
    public Fine toDomainModel() {
        return Fine.builder()
                .id(id)
                .borrowRecordId(borrowRecordId)
                .memberId(memberId)
                .amount(amount)
                .fineType(fineType)
                .status(status)
                .reason(reason)
                .createdAt(createdAt)
                .paidAt(paidAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    public static FineJpaEntity fromDomainModel(Fine fine) {
        return FineJpaEntity.builder()
                .id(fine.getId())
                .borrowRecordId(fine.getBorrowRecordId())
                .memberId(fine.getMemberId())
                .amount(fine.getAmount())
                .fineType(fine.getFineType())
                .status(fine.getStatus())
                .reason(fine.getReason())
                .createdAt(fine.getCreatedAt())
                .paidAt(fine.getPaidAt())
                .updatedAt(fine.getUpdatedAt())
                .build();
    }
}
