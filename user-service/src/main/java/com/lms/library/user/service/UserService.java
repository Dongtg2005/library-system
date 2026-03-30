package com.lms.library.user.service;

import com.lms.library.user.dto.CreateUserRequest;
import com.lms.library.user.dto.UpdateRoleRequest;
import com.lms.library.user.dto.UpdateStatusRequest;
import com.lms.library.user.dto.UpdateUserRequest;
import com.lms.library.user.dto.UserResponse;
import com.lms.library.user.entity.UserProfile;
import com.lms.library.user.exception.ForbiddenOperationException;
import com.lms.library.user.exception.UserAlreadyExistsException;
import com.lms.library.user.exception.UserNotFoundException;
import com.lms.library.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UserProfile.Role requesterRole) {
        requireAdmin(requesterRole);

        userProfileRepository.findByAuthUserIdAndDeletedFalse(request.getAuthUserId())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("authUserId already exists: " + request.getAuthUserId());
                });

        userProfileRepository.findByEmailAndDeletedFalse(request.getEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("email already exists: " + request.getEmail());
                });

        UserProfile profile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .build();

        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id, Long requesterAuthUserId, UserProfile.Role requesterRole) {
        UserProfile profile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isOwner = profile.getAuthUserId().equals(requesterAuthUserId);
        boolean elevated = requesterRole == UserProfile.Role.ADMIN || requesterRole == UserProfile.Role.LIBRARIAN;
        if (!isOwner && !elevated) {
            throw new ForbiddenOperationException("You can only access your own profile");
        }

        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll(UserProfile.Role requesterRole) {
        if (requesterRole != UserProfile.Role.ADMIN && requesterRole != UserProfile.Role.LIBRARIAN) {
            throw new ForbiddenOperationException("Only ADMIN or LIBRARIAN can view all users");
        }
        return userProfileRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateProfile(Long id,
                                      UpdateUserRequest request,
                                      Long requesterAuthUserId,
                                      UserProfile.Role requesterRole) {
        UserProfile profile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isOwner = profile.getAuthUserId().equals(requesterAuthUserId);
        if (!isOwner && requesterRole != UserProfile.Role.ADMIN) {
            throw new ForbiddenOperationException("Only profile owner or ADMIN can update profile");
        }

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request, UserProfile.Role requesterRole) {
        requireAdmin(requesterRole);

        UserProfile profile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        profile.setRole(request.getRole());
        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request, UserProfile.Role requesterRole) {
        if (requesterRole != UserProfile.Role.ADMIN && requesterRole != UserProfile.Role.LIBRARIAN) {
            throw new ForbiddenOperationException("Only ADMIN or LIBRARIAN can change member status");
        }

        UserProfile profile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        profile.setMemberStatus(request.getMemberStatus());
        return toResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public void deleteUser(Long id, UserProfile.Role requesterRole) {
        requireAdmin(requesterRole);

        UserProfile profile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        profile.setDeleted(true);
        userProfileRepository.save(profile);
    }

    private void requireAdmin(UserProfile.Role requesterRole) {
        if (requesterRole != UserProfile.Role.ADMIN) {
            throw new ForbiddenOperationException("Only ADMIN can perform this operation");
        }
    }

    private UserResponse toResponse(UserProfile profile) {
        return UserResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .role(profile.getRole())
                .memberStatus(profile.getMemberStatus())
                .totalBooksBorrowed(profile.getTotalBooksBorrowed())
                .currentBooksBorrowed(profile.getCurrentBooksBorrowed())
                .outstandingFines(profile.getOutstandingFines())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
