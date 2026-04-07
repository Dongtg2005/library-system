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

    // Builder pattern implementation
    public static FineBuilder builder() {
        return new FineBuilder();
    }

    public static class FineBuilder {
        private UUID id;
        private BorrowRecord borrowRecord;
        private UUID memberId;
        private FineType fineType;
        private BigDecimal amount;
        private BigDecimal dailyRate;
        private BigDecimal maxFineAmount;
        private Integer daysOverdue;
        private String reason;
        private FineStatus status = FineStatus.PENDING;
        private String paymentMethod;
        private ZonedDateTime paymentDate;
        private UUID waivedById;
        private ZonedDateTime waivedDate;
        private String waiverReason;

        public FineBuilder id(UUID id) { this.id = id; return this; }
        public FineBuilder borrowRecord(BorrowRecord borrowRecord) { this.borrowRecord = borrowRecord; return this; }
        public FineBuilder memberId(UUID memberId) { this.memberId = memberId; return this; }
        public FineBuilder fineType(FineType fineType) { this.fineType = fineType; return this; }
        public FineBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public FineBuilder dailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; return this; }
        public FineBuilder maxFineAmount(BigDecimal maxFineAmount) { this.maxFineAmount = maxFineAmount; return this; }
        public FineBuilder daysOverdue(Integer daysOverdue) { this.daysOverdue = daysOverdue; return this; }
        public FineBuilder reason(String reason) { this.reason = reason; return this; }
        public FineBuilder status(FineStatus status) { this.status = status; return this; }
        public FineBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public FineBuilder paymentDate(ZonedDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
        public FineBuilder waivedById(UUID waivedById) { this.waivedById = waivedById; return this; }
        public FineBuilder waivedDate(ZonedDateTime waivedDate) { this.waivedDate = waivedDate; return this; }
        public FineBuilder waiverReason(String waiverReason) { this.waiverReason = waiverReason; return this; }

        public Fine build() {
            Fine fine = new Fine();
            fine.id = this.id;
            fine.borrowRecord = this.borrowRecord;
            fine.memberId = this.memberId;
            fine.fineType = this.fineType;
            fine.amount = this.amount;
            fine.dailyRate = this.dailyRate;
            fine.maxFineAmount = this.maxFineAmount;
            fine.daysOverdue = this.daysOverdue;
            fine.reason = this.reason;
            fine.status = this.status;
            fine.paymentMethod = this.paymentMethod;
            fine.paymentDate = this.paymentDate;
            fine.waivedById = this.waivedById;
            fine.waivedDate = this.waivedDate;
            fine.waiverReason = this.waiverReason;
            return fine;
        }
    }

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BorrowRecord getBorrowRecord() { return borrowRecord; }
    public void setBorrowRecord(BorrowRecord borrowRecord) { this.borrowRecord = borrowRecord; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public FineType getFineType() { return fineType; }
    public void setFineType(FineType fineType) { this.fineType = fineType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }    
    public BigDecimal getMaxFineAmount() { return maxFineAmount; }
    public void setMaxFineAmount(BigDecimal maxFineAmount) { this.maxFineAmount = maxFineAmount; }
    public Integer getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue) { this.daysOverdue = daysOverdue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public ZonedDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(ZonedDateTime paymentDate) { this.paymentDate = paymentDate; }
    public UUID getWaivedById() { return waivedById; }
    public void setWaivedById(UUID waivedById) { this.waivedById = waivedById; }
    public ZonedDateTime getWaivedDate() { return waivedDate; }
    public void setWaivedDate(ZonedDateTime waivedDate) { this.waivedDate = waivedDate; }
    public String getWaiverReason() { return waiverReason; }
    public void setWaiverReason(String waiverReason) { this.waiverReason = waiverReason; }
}
