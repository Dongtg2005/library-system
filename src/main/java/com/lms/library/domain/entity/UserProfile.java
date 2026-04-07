package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    
    private Long id;
    private Long authUserId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private MemberStatus memberStatus;
    private Integer totalBooksBorrowed;
    private Integer currentBooksBorrowed;
    private BigDecimal totalFines;
    private BigDecimal outstandingFines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN,
        LIBRARIAN,
        USER
    }
    
    public enum MemberStatus {
        ACTIVE,
        SUSPENDED,
        INACTIVE
    }
    
    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(this.memberStatus);
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
        this.totalBooksBorrowed++;
        this.currentBooksBorrowed++;
    }
    
    public void decrementBorrowedCount() {
        if (this.currentBooksBorrowed > 0) {
            this.currentBooksBorrowed--;
        }
    }
}
