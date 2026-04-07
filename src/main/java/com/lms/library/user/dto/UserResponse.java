package com.lms.library.user.dto;

import com.lms.library.user.entity.UserProfile;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private Long authUserId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserProfile.Role role;
    private UserProfile.MemberStatus memberStatus;
    private Integer totalBooksBorrowed;
    private Integer currentBooksBorrowed;
    private BigDecimal outstandingFines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
