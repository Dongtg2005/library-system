package com.lms.library.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull(message = "Review ID is required")
    private Long reviewId;

    private Long parentId;

    @NotBlank(message = "Comment content is required")
    private String content;
}
