package com.lms.library.domain.repository;

import com.lms.library.domain.entity.UserProfile;
import java.util.List;
import java.util.Optional;

public interface UserProfileRepository {
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    Optional<UserProfile> findByEmail(String email);
    UserProfile save(UserProfile userProfile);
    Optional<UserProfile> findById(Long id);
    List<UserProfile> findAll();
    void deleteById(Long id);
}
