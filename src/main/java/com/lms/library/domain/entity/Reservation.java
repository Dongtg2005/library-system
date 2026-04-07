package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    private UUID id;
    private Long userId;
    private UUID bookId;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private ReservationStatus status;
    private Integer priority; // 1=Normal, 2=High, 3=Urgent
    private LocalDateTime createdAt;
    
    public enum ReservationStatus {
        ACTIVE, FULFILLED, CANCELLED, EXPIRED
    }
    
    public boolean isActive() {
        return ReservationStatus.ACTIVE.equals(status);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canBeFulfilled() {
        return isActive() && !isExpired();
    }
}
