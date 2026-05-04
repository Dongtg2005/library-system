package com.lms.library.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @NotNull(message = "Vote type is required")
    private VoteType voteType;

    public enum VoteType {
        LIKE, DISLIKE
    }
}
