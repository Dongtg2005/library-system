package com.lms.library.user.dto;

import com.lms.library.user.entity.UserProfile;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "role is required")
    private UserProfile.Role role;
}
