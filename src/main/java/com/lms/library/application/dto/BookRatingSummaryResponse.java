package com.lms.library.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRatingSummaryResponse {
    private UUID bookId;
    private BigDecimal averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution;
}
