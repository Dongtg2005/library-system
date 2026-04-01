package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.ExtensionStatus;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrow_extensions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowExtension extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @Column(name = "extension_number", nullable = false)
    @Builder.Default
    private Integer extensionNumber = 1;

    @Column(name = "requested_date", nullable = false)
    @Builder.Default
    private ZonedDateTime requestedDate = ZonedDateTime.now();

    @Column(name = "approved_date")
    private ZonedDateTime approvedDate;

    @Column(name = "approved_by_id")
    private UUID approvedById;

    @Column(name = "old_due_date", nullable = false)
    private LocalDate oldDueDate;

    @Column(name = "new_due_date", nullable = false)
    private LocalDate newDueDate;

    @Column(name = "extension_days", nullable = false)
    private Integer extensionDays;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ExtensionStatus status = ExtensionStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "is_auto_approved")
    @Builder.Default
    private Boolean isAutoApproved = false;
}
