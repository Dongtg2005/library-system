package com.lms.library.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoverUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    private String url;
}
