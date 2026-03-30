package com.lms.library.user.dto;

import com.lms.library.user.entity.UserProfile;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "memberStatus is required")
    private UserProfile.MemberStatus memberStatus;
}
