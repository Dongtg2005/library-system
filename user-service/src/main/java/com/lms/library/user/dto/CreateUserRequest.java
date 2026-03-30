package com.lms.library.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotNull(message = "authUserId is required")
    private Long authUserId;

    @NotBlank(message = "email is required")
    @Email(message = "email is invalid")
    private String email;

    @NotBlank(message = "fullName is required")
    private String fullName;

    @Pattern(regexp = "^$|^\\+?[0-9]{8,15}$", message = "phoneNumber is invalid")
    private String phoneNumber;
}
