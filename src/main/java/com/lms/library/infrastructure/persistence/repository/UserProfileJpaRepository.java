package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.infrastructure.persistence.jpa.UserProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, Long> {
    Optional<UserProfileJpaEntity> findByAuthUserId(Long authUserId);
    Optional<UserProfileJpaEntity> findByEmail(String email);
}
