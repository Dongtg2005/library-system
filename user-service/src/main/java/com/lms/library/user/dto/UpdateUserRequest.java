package com.lms.library.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @Pattern(regexp = "^$|^\\+?[0-9]{8,15}$", message = "phoneNumber is invalid")
    private String phoneNumber;
}
