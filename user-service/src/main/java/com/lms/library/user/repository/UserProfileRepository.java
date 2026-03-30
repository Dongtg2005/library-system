package com.lms.library.user.repository;

import com.lms.library.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByIdAndDeletedFalse(Long id);

    Optional<UserProfile> findByAuthUserIdAndDeletedFalse(Long authUserId);

    Optional<UserProfile> findByEmailAndDeletedFalse(String email);

    List<UserProfile> findByRoleAndDeletedFalse(UserProfile.Role role);

    List<UserProfile> findByMemberStatusAndDeletedFalse(UserProfile.MemberStatus memberStatus);
}
