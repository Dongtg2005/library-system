package com.lms.library.infrastructure.persistence.jpa;

import com.lms.library.domain.entity.UserProfile;
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
public class UserProfileJpaEntity {

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
    private UserProfile.Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserProfile.MemberStatus memberStatus;

    @Column(nullable = false)
    private Integer totalBooksBorrowed;

    @Column(nullable = false)
    private Integer currentBooksBorrowed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFines;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal outstandingFines;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;
    
    public UserProfile toDomainModel() {
        return UserProfile.builder()
                .id(id)
                .authUserId(authUserId)
                .email(email)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .role(role)
                .memberStatus(memberStatus)
                .totalBooksBorrowed(totalBooksBorrowed)
                .currentBooksBorrowed(currentBooksBorrowed)
                .totalFines(totalFines)
                .outstandingFines(outstandingFines)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    public static UserProfileJpaEntity fromDomainModel(UserProfile userProfile) {
        return UserProfileJpaEntity.builder()
                .id(userProfile.getId())
                .authUserId(userProfile.getAuthUserId())
                .email(userProfile.getEmail())
                .fullName(userProfile.getFullName())
                .phoneNumber(userProfile.getPhoneNumber())
                .role(userProfile.getRole())
                .memberStatus(userProfile.getMemberStatus())
                .totalBooksBorrowed(userProfile.getTotalBooksBorrowed())
                .currentBooksBorrowed(userProfile.getCurrentBooksBorrowed())
                .totalFines(userProfile.getTotalFines())
                .outstandingFines(userProfile.getOutstandingFines())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}
