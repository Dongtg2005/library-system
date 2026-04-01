package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.EventStatus;
import com.lms.library.borrow.entity.enums.EventType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrow_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id")
    private BorrowRecord borrowRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 50)
    private EventStatus eventStatus;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "book_id")
    private UUID bookId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "saga_id", length = 100)
    private String sagaId;

    @Column(name = "triggered_by_id")
    private UUID triggeredById;

    @Column(name = "triggered_at", nullable = false)
    @Builder.Default
    private ZonedDateTime triggeredAt = ZonedDateTime.now();

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
