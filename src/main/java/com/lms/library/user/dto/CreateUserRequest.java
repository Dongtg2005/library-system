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

    public Long getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(Long authUserId) {
        this.authUserId = authUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static CreateUserRequestBuilder builder() {
        return new CreateUserRequestBuilder();
    }

    public static class CreateUserRequestBuilder {
        private Long authUserId;
        private String email;
        private String fullName;
        private String phoneNumber;

        public CreateUserRequestBuilder authUserId(Long authUserId) {
            this.authUserId = authUserId;
            return this;
        }

        public CreateUserRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CreateUserRequestBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public CreateUserRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public CreateUserRequest build() {
            CreateUserRequest request = new CreateUserRequest();
            request.authUserId = this.authUserId;
            request.email = this.email;
            request.fullName = this.fullName;
            request.phoneNumber = this.phoneNumber;
            return request;
        }
    }
}
