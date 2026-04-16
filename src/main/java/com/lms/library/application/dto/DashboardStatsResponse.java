package com.lms.library.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalBooks;
    private long totalBorrowed;
    private long totalOverdue;
    
    // For the chart
    private List<WeeklyActivity> weeklyActivities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyActivity {
        private String name; // e.g. "Mon", "Tue"
        private long users;
        private long books;
        private long borrows;
    }
}
