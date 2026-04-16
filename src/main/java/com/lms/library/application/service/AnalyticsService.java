package com.lms.library.application.service;

import com.lms.library.application.dto.DashboardStatsResponse;
import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching real-time dashboard analytics");

        long totalUsers = userRepository.count();
        long totalBooks = bookRepository.count();
        long totalBorrowed = borrowRecordRepository.countByBorrowStatus(BorrowRecord.BorrowStatus.ACTIVE)
                + borrowRecordRepository.countByBorrowStatus(BorrowRecord.BorrowStatus.OVERDUE);
        long totalOverdue = borrowRecordRepository.countByBorrowStatus(BorrowRecord.BorrowStatus.OVERDUE);

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalBooks(totalBooks)
                .totalBorrowed(totalBorrowed)
                .totalOverdue(totalOverdue)
                .weeklyActivities(generateWeeklyStats())
                .build();
    }

    private List<DashboardStatsResponse.WeeklyActivity> generateWeeklyStats() {
        List<DashboardStatsResponse.WeeklyActivity> activities = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Generate stats for last 7 days
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            // In a real production app, we would use native SQL or JPQL with GROUP BY
            // For this project, we'll simulate or do simple counts for the demo
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long usersAtDay = userRepository.countByCreatedAtBefore(endOfDay);
            long booksAtDay = bookRepository.countByCreatedAtBefore(endOfDay);
            long borrowsAtDay = borrowRecordRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            activities.add(DashboardStatsResponse.WeeklyActivity.builder()
                    .name(dayName)
                    .users(usersAtDay)
                    .books(booksAtDay)
                    .borrows(borrowsAtDay)
                    .build());
        }

        return activities;
    }
}
