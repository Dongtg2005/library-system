package com.lms.library.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "user_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Type(JsonType.class)
    @Column(name = "favorite_genres", columnDefinition = "jsonb")
    private List<String> favoriteGenres;

    @Type(JsonType.class)
    @Column(name = "reading_preferences", columnDefinition = "jsonb")
    private Map<String, Object> readingPreferences;

    @Type(JsonType.class)
    @Column(name = "notification_settings", columnDefinition = "jsonb")
    private Map<String, Object> notificationSettings;

    @Type(JsonType.class)
    @Column(name = "privacy_settings", columnDefinition = "jsonb")
    private Map<String, Object> privacySettings;

    @Column(name = "total_books_read")
    private Integer totalBooksRead = 0;

    @Column(name = "total_pages_read")
    private Long totalPagesRead = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level", length = 20)
    private MembershipLevel membershipLevel = MembershipLevel.BRONZE;

    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", length = 20)
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    @Column(name = "total_books_borrowed")
    private Integer totalBooksBorrowed = 0;

    @Column(name = "current_books_borrowed")
    private Integer currentBooksBorrowed = 0;

    @Column(name = "total_fines", precision = 10, scale = 2)
    private BigDecimal totalFines = BigDecimal.ZERO;

    @Column(name = "outstanding_fines", precision = 10, scale = 2)
    private BigDecimal outstandingFines = BigDecimal.ZERO;

    @Column(name = "card_expiry_date")
    private LocalDate cardExpiryDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (memberStatus == null) memberStatus = MemberStatus.ACTIVE;
        if (membershipLevel == null) membershipLevel = MembershipLevel.BRONZE;
        if (totalBooksRead == null) totalBooksRead = 0;
        if (totalPagesRead == null) totalPagesRead = 0L;
        if (points == null) points = 0;
        if (totalBooksBorrowed == null) totalBooksBorrowed = 0;
        if (currentBooksBorrowed == null) currentBooksBorrowed = 0;
        if (totalFines == null) totalFines = BigDecimal.ZERO;
        if (outstandingFines == null) outstandingFines = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MemberStatus {
        ACTIVE, SUSPENDED, INACTIVE
    }

    public enum MembershipLevel {
        BRONZE, SILVER, GOLD, PLATINUM
    }

    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(this.memberStatus);
    }

    public boolean isCardExpired() {
        return cardExpiryDate != null && LocalDate.now().isAfter(cardExpiryDate);
    }

    public boolean isSuspended() {
        return MemberStatus.SUSPENDED.equals(this.memberStatus);
    }

    public boolean hasOutstandingFines() {
        return outstandingFines != null && outstandingFines.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean canBorrowBooks() {
        return isActive() && !hasOutstandingFines();
    }

    public void incrementBorrowedCount() {
        if (this.totalBooksBorrowed == null) this.totalBooksBorrowed = 0;
        if (this.currentBooksBorrowed == null) this.currentBooksBorrowed = 0;
        this.totalBooksBorrowed++;
        this.currentBooksBorrowed++;
    }

    public void decrementBorrowedCount() {
        if (this.currentBooksBorrowed != null && this.currentBooksBorrowed > 0) {
            this.currentBooksBorrowed--;
        }
    }

    public void addPoints(int pointsToAdd) {
        if (this.points == null) this.points = 0;
        this.points += pointsToAdd;
    }

    public void incrementBooksRead() {
        if (this.totalBooksRead == null) this.totalBooksRead = 0;
        this.totalBooksRead++;
    }
}
