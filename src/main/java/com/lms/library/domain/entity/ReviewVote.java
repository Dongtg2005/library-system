package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVote {
    
    private Long id;
    private Long reviewId;
    private Long userId;
    private Boolean helpful;
    private LocalDateTime createdAt;
    
    public boolean isHelpful() {
        return Boolean.TRUE.equals(helpful);
    }
}
