package com.lms.library.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long authUserId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    @Column(nullable = false)
    private Integer totalBooksBorrowed;

    @Column(nullable = false)
    private Integer currentBooksBorrowed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal outstandingFines;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.memberStatus == null) {
            this.memberStatus = MemberStatus.ACTIVE;
        }
        if (this.totalBooksBorrowed == null) {
            this.totalBooksBorrowed = 0;
        }
        if (this.currentBooksBorrowed == null) {
            this.currentBooksBorrowed = 0;
        }
        if (this.outstandingFines == null) {
            this.outstandingFines = BigDecimal.ZERO;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Role {
        ADMIN,
        LIBRARIAN,
        USER
    }

    public enum MemberStatus {
        ACTIVE,
        SUSPENDED,
        BANNED
    }
}