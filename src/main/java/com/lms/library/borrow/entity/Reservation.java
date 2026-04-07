package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.NotificationStatus;
import com.lms.library.borrow.entity.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "reservation_date", nullable = false)
    @Builder.Default
    private ZonedDateTime reservationDate = ZonedDateTime.now();

    @Column(name = "reservation_expiry_date", nullable = false)
    private ZonedDateTime reservationExpiryDate;

    @Column(name = "queue_position", nullable = false)
    @Builder.Default
    private Integer queuePosition = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", length = 50)
    @Builder.Default
    private NotificationStatus notificationStatus = NotificationStatus.NOT_NOTIFIED;

    @Column(name = "notified_at")
    private ZonedDateTime notifiedAt;

    @Column(name = "fulfilled_date")
    private ZonedDateTime fulfilledDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
