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
public class PopularBook {
    
    private UUID bookId;
    private Integer borrowCount;
    private Integer viewCount;
    private Integer searchCount;
    private LocalDateTime lastCalculated;
    
    public void incrementBorrowCount() {
        if (borrowCount == null) borrowCount = 0;
        borrowCount++;
    }
    
    public void incrementViewCount() {
        if (viewCount == null) viewCount = 0;
        viewCount++;
    }
    
    public void incrementSearchCount() {
        if (searchCount == null) searchCount = 0;
        searchCount++;
    }
    
    public int getPopularityScore() {
        // Weighted score: borrow (3x), view (1x), search (2x)
        return (borrowCount != null ? borrowCount * 3 : 0) +
               (viewCount != null ? viewCount : 0) +
               (searchCount != null ? searchCount * 2 : 0);
    }
}
