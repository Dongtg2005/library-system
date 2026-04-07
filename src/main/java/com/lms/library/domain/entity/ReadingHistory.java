package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingHistory {
    private Long id;
    private Long userId;
    private UUID bookId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer pagesRead;
    private Integer ratingGiven;
    private Boolean reviewGiven;
    private LocalDateTime createdAt;
    
    public boolean isCompleted() {
        return finishedAt != null;
    }
    
    public boolean isCurrentlyReading() {
        return startedAt != null && finishedAt == null;
    }
    
    public long getReadingDurationInDays() {
        if (startedAt == null || finishedAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startedAt, finishedAt);
    }
}
