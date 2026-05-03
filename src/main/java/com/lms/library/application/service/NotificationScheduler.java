package com.lms.library.application.service;

import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Book;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkOverdueAndDueSoonBooks() {
        log.info("Starting scheduled check for overdue and due soon books");

        // Check for overdue books
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords();
        log.info("Found {} overdue records", overdueRecords.size());

        for (BorrowRecord record : overdueRecords) {
            try {
                Book book = bookRepository.findById(record.getBookId()).orElse(null);
                if (book == null) {
                    log.warn("Book not found for borrow record: {}", record.getId());
                    continue;
                }

                long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), java.time.LocalDate.now());
                notificationService.notifyUserOverdue(
                    record.getMemberId(),
                    book.getTitle(),
                    record.getBookId(),
                    (int) overdueDays
                );
            } catch (Exception e) {
                log.error("Failed to create overdue notification for record: {}", record.getId(), e);
            }
        }

        // Check for due soon books (within 3 days)
        List<BorrowRecord> dueSoonRecords = borrowRecordRepository.findDueSoonRecords();
        log.info("Found {} due soon records", dueSoonRecords.size());

        for (BorrowRecord record : dueSoonRecords) {
            try {
                Book book = bookRepository.findById(record.getBookId()).orElse(null);
                if (book == null) {
                    log.warn("Book not found for borrow record: {}", record.getId());
                    continue;
                }

                notificationService.notifyUserDueSoon(
                    record.getMemberId(),
                    book.getTitle(),
                    record.getBookId(),
                    record.getDueDate().atStartOfDay()
                );
            } catch (Exception e) {
                log.error("Failed to create due soon notification for record: {}", record.getId(), e);
            }
        }

        log.info("Completed scheduled check for overdue and due soon books");
    }
}
