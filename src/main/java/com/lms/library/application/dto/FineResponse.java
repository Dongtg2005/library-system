package com.lms.library.application.dto;

import com.lms.library.domain.entity.Fine;
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
public class FineResponse {
    private UUID id;
    private UUID borrowRecordId;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private Fine.FineType fineType;
    private Fine.FineStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public static FineResponse from(Fine fine) {
        return from(fine, null);
    }

    public static FineResponse from(Fine fine, String memberName) {
        return FineResponse.builder()
                .id(fine.getId())
                .borrowRecordId(fine.getBorrowRecordId())
                .memberId(fine.getMemberId())
                .memberName(memberName)
                .amount(fine.getAmount())
                .fineType(fine.getFineType())
                .status(fine.getStatus())
                .reason(fine.getReason())
                .createdAt(fine.getCreatedAt())
                .paidAt(fine.getPaidAt())
                .build();
    }
}
