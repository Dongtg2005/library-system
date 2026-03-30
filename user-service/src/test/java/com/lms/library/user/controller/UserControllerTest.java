package com.lms.library.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.library.user.dto.CreateUserRequest;
import com.lms.library.user.dto.UpdateRoleRequest;
import com.lms.library.user.dto.UserResponse;
import com.lms.library.user.entity.UserProfile;
import com.lms.library.user.exception.ForbiddenOperationException;
import com.lms.library.user.exception.GlobalExceptionHandler;
import com.lms.library.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreated_whenRequestIsValid() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setAuthUserId(300L);
        request.setEmail("new@example.com");
        request.setFullName("New Reader");
        request.setPhoneNumber("+84911112222");

        UserResponse response = UserResponse.builder()
                .id(10L)
                .authUserId(300L)
                .email("new@example.com")
                .fullName("New Reader")
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .build();

        when(userService.createUser(any(CreateUserRequest.class), eq(UserProfile.Role.ADMIN))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void createUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setAuthUserId(301L);
        request.setEmail("invalid-email");
        request.setFullName("");
        request.setPhoneNumber("abc");

        mockMvc.perform(post("/users")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void updateRole_shouldReturnForbidden_whenServiceThrowsForbidden() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(UserProfile.Role.ADMIN);

        doThrow(new ForbiddenOperationException("Only ADMIN can perform this operation"))
                .when(userService)
                .updateRole(eq(1L), any(UpdateRoleRequest.class), eq(UserProfile.Role.USER));

        mockMvc.perform(patch("/users/1/role")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getById_shouldReturnOk_whenOwnerRequestsOwnProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .authUserId(101L)
                .email("reader@example.com")
                .fullName("Reader User")
                .role(UserProfile.Role.USER)
                .memberStatus(UserProfile.MemberStatus.ACTIVE)
                .build();

        when(userService.getById(1L, 101L, UserProfile.Role.USER)).thenReturn(response);

        mockMvc.perform(get("/users/1")
                        .header("X-Auth-User-Id", "101")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("reader@example.com"));
    }
}