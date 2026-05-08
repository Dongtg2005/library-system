package com.lms.library.application.dto;

import com.lms.library.domain.entity.Reservation;
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
public class ReservationResponse {
    private UUID id;
    private Long userId;
    private String userName;
    private UUID bookId;
    private String bookTitle;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private Reservation.ReservationStatus status;
    private Integer priority;
    private Boolean notificationSent;
    private LocalDateTime fulfilledAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime onHoldAt;
    private LocalDateTime holdExpiresAt;
    private String notes;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation, String bookTitle, String userName) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .userName(userName)
                .bookId(reservation.getBookId())
                .bookTitle(bookTitle)
                .reservedAt(reservation.getReservedAt())
                .expiresAt(reservation.getExpiresAt())
                .status(reservation.getStatus())
                .priority(reservation.getPriority())
                .notificationSent(reservation.getNotificationSent())
                .fulfilledAt(reservation.getFulfilledAt())
                .cancelledAt(reservation.getCancelledAt())
                .onHoldAt(reservation.getOnHoldAt())
                .holdExpiresAt(reservation.getHoldExpiresAt())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
