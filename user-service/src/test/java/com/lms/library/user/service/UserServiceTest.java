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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    private UserProfile existingUser;

    @BeforeEach
    void setUp() {
        existingUser = UserProfile.builder()
                .id(1L)
                .authUserId(101L)
                .email("reader@example.com")
                .fullName("Reader User")
                .phoneNumber("+84901234567")
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .totalBooksBorrowed(3)
                .currentBooksBorrowed(1)
                .outstandingFines(BigDecimal.ZERO)
                .deleted(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void createUser_shouldSucceed_whenRequesterIsAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setAuthUserId(200L);
        request.setEmail("newuser@example.com");
        request.setFullName("New User");
        request.setPhoneNumber("+84987654321");

        UserProfile saved = UserProfile.builder()
                .id(2L)
                .authUserId(200L)
                .email("newuser@example.com")
                .fullName("New User")
                .phoneNumber("+84987654321")
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .totalBooksBorrowed(0)
                .currentBooksBorrowed(0)
                .outstandingFines(BigDecimal.ZERO)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userProfileRepository.findByAuthUserIdAndDeletedFalse(200L)).thenReturn(Optional.empty());
        when(userProfileRepository.findByEmailAndDeletedFalse("newuser@example.com")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(saved);

        UserResponse result = userService.createUser(request, UserProfile.Role.ADMIN);

        assertEquals(2L, result.getId());
        assertEquals(UserProfile.Role.USER, result.getRole());
        assertEquals(UserProfile.MemberStatus.ACTIVE, result.getMemberStatus());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createUser_shouldFail_whenRequesterIsNotAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setAuthUserId(201L);
        request.setEmail("blocked@example.com");
        request.setFullName("Blocked User");

        assertThrows(ForbiddenOperationException.class,
                () -> userService.createUser(request, UserProfile.Role.USER));

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void createUser_shouldFail_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setAuthUserId(202L);
        request.setEmail("reader@example.com");
        request.setFullName("Duplicate User");

        when(userProfileRepository.findByAuthUserIdAndDeletedFalse(202L)).thenReturn(Optional.empty());
        when(userProfileRepository.findByEmailAndDeletedFalse("reader@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request, UserProfile.Role.ADMIN));

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void getById_shouldSucceed_whenRequesterIsOwner() {
        when(userProfileRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));

        UserResponse result = userService.getById(1L, 101L, UserProfile.Role.USER);

        assertEquals(1L, result.getId());
        assertEquals("reader@example.com", result.getEmail());
    }

    @Test
    void getById_shouldFail_whenRequesterIsNeitherOwnerNorElevated() {
        when(userProfileRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));

        assertThrows(ForbiddenOperationException.class,
                () -> userService.getById(1L, 999L, UserProfile.Role.USER));
    }

    @Test
    void getAll_shouldFail_whenRequesterIsUser() {
        assertThrows(ForbiddenOperationException.class,
                () -> userService.getAll(UserProfile.Role.USER));
    }

    @Test
    void updateRole_shouldSucceed_whenRequesterIsAdmin() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(UserProfile.Role.LIBRARIAN);

        when(userProfileRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.updateRole(1L, request, UserProfile.Role.ADMIN);

        assertEquals(UserProfile.Role.LIBRARIAN, result.getRole());
    }

    @Test
    void updateStatus_shouldSucceed_whenRequesterIsLibrarian() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setMemberStatus(UserProfile.MemberStatus.SUSPENDED);

        when(userProfileRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.updateStatus(1L, request, UserProfile.Role.LIBRARIAN);

        assertEquals(UserProfile.MemberStatus.SUSPENDED, result.getMemberStatus());
    }

    @Test
    void deleteUser_shouldSoftDelete_whenRequesterIsAdmin() {
        when(userProfileRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(1L, UserProfile.Role.ADMIN);

        assertFalse(Boolean.FALSE.equals(existingUser.getDeleted()));
        verify(userProfileRepository).save(existingUser);
    }

    @Test
    void updateProfile_shouldFail_whenUserNotFound() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Name");
        request.setPhoneNumber("+84999999999");

        when(userProfileRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateProfile(999L, request, 101L, UserProfile.Role.ADMIN));
    }

    @Test
    void getAll_shouldReturnUsers_whenRequesterIsAdmin() {
        UserProfile deletedUser = UserProfile.builder()
                .id(5L)
                .authUserId(105L)
                .email("deleted@example.com")
                .fullName("Deleted")
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .deleted(true)
                .totalBooksBorrowed(0)
                .currentBooksBorrowed(0)
                .outstandingFines(BigDecimal.ZERO)
                .build();

        when(userProfileRepository.findAll()).thenReturn(List.of(existingUser, deletedUser));

        List<UserResponse> result = userService.getAll(UserProfile.Role.ADMIN);

        assertEquals(1, result.size());
        assertEquals(existingUser.getId(), result.getFirst().getId());
    }
}