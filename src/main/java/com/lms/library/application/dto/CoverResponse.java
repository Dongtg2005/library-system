package com.lms.library.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverResponse {
    private boolean success;
    private String coverUrl;
    private LocalDateTime updatedAt;
}
