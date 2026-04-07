package com.lms.library.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAuthUserId() { return authUserId; }
    public void setAuthUserId(Long authUserId) { this.authUserId = authUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public MemberStatus getMemberStatus() { return memberStatus; }
    public void setMemberStatus(MemberStatus memberStatus) { this.memberStatus = memberStatus; }
    public Integer getTotalBooksBorrowed() { return totalBooksBorrowed; }
    public void setTotalBooksBorrowed(Integer totalBooksBorrowed) { this.totalBooksBorrowed = totalBooksBorrowed; }
    public Integer getCurrentBooksBorrowed() { return currentBooksBorrowed; }
    public void setCurrentBooksBorrowed(Integer currentBooksBorrowed) { this.currentBooksBorrowed = currentBooksBorrowed; }
    public BigDecimal getOutstandingFines() { return outstandingFines; }
    public void setOutstandingFines(BigDecimal outstandingFines) { this.outstandingFines = outstandingFines; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static UserProfileBuilder builder() {
        return new UserProfileBuilder();
    }

    public static class UserProfileBuilder {
        private Long id;
        private Long authUserId;
        private String email;
        private String fullName;
        private String phoneNumber;
        private Role role;
        private MemberStatus memberStatus;
        private Integer totalBooksBorrowed;
        private Integer currentBooksBorrowed;
        private BigDecimal outstandingFines;
        private Boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserProfileBuilder id(Long id) { this.id = id; return this; }
        public UserProfileBuilder authUserId(Long authUserId) { this.authUserId = authUserId; return this; }
        public UserProfileBuilder email(String email) { this.email = email; return this; }
        public UserProfileBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserProfileBuilder phoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; return this; }
        public UserProfileBuilder role(Role role) { this.role = role; return this; }
        public UserProfileBuilder memberStatus(MemberStatus memberStatus) { this.memberStatus = memberStatus; return this; }
        public UserProfileBuilder totalBooksBorrowed(Integer totalBooksBorrowed) { this.totalBooksBorrowed = totalBooksBorrowed; return this; }
        public UserProfileBuilder currentBooksBorrowed(Integer currentBooksBorrowed) { this.currentBooksBorrowed = currentBooksBorrowed; return this; }
        public UserProfileBuilder outstandingFines(BigDecimal outstandingFines) { this.outstandingFines = outstandingFines; return this; }
        public UserProfileBuilder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public UserProfileBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserProfileBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserProfile build() {
            UserProfile profile = new UserProfile();
            profile.id = this.id;
            profile.authUserId = this.authUserId;
            profile.email = this.email;
            profile.fullName = this.fullName;
            profile.phoneNumber = this.phoneNumber;
            profile.role = this.role;
            profile.memberStatus = this.memberStatus;
            profile.totalBooksBorrowed = this.totalBooksBorrowed;
            profile.currentBooksBorrowed = this.currentBooksBorrowed;
            profile.outstandingFines = this.outstandingFines;
            profile.deleted = this.deleted;
            profile.createdAt = this.createdAt;
            profile.updatedAt = this.updatedAt;
            return profile;
        }
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