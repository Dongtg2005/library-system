package com.lms.library.application.service;

import com.lms.library.application.dto.FineResponse;
import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Fine;
import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.FineRepository;
import com.lms.library.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineService {

    private final FineRepository fineRepository;
    private final UserProfileRepository userProfileRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Transactional(readOnly = true)
    public List<FineResponse> getAllFines() {
        log.info("Fetching all fines");
        return fineRepository.findAll().stream()
                .map(this::toFineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FineResponse> getMemberFines(Long memberId) {
        log.info("Fetching fines for member: {}", memberId);
        return fineRepository.findByMemberId(memberId).stream()
                .map(this::toFineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FineResponse> getFinesByStatus(Fine.FineStatus status) {
        log.info("Fetching fines with status: {}", status);
        return fineRepository.findByStatus(status).stream()
                .map(this::toFineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FineResponse getFineById(UUID fineId) {
        log.info("Fetching fine by ID: {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with ID: " + fineId));
        return toFineResponse(fine);
    }

    @Transactional
    public FineResponse payFine(UUID fineId) {
        log.info("Processing payment for fine ID: {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with ID: " + fineId));

        if (!fine.isPending()) {
            throw new IllegalStateException("Fine is not in PENDING status and cannot be paid. Current status: " + fine.getStatus());
        }

        fine.markAsPaid();
        fine = fineRepository.save(fine);

        // Update UserProfile outstanding fines
        UserProfile userProfile = userProfileRepository.findByUserId(fine.getMemberId())
                .orElse(null);
        if (userProfile != null) {
            BigDecimal newOutstanding = userProfile.getOutstandingFines().subtract(fine.getAmount());
            if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                newOutstanding = BigDecimal.ZERO;
            }
            userProfile.setOutstandingFines(newOutstanding);
            userProfileRepository.save(userProfile);
        }

        // Update BorrowRecord fine paid status
        BorrowRecord record = borrowRecordRepository.findById(fine.getBorrowRecordId()).orElse(null);
        if (record != null) {
            record.setFinePaid(true);
            record.setFinePaidAt(LocalDateTime.now());
            borrowRecordRepository.save(record);
        }

        log.info("Fine ID: {} paid successfully", fineId);
        return toFineResponse(fine);
    }

    @Transactional
    public FineResponse waiveFine(UUID fineId) {
        log.info("Waiving fine ID: {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with ID: " + fineId));

        if (!fine.isPending()) {
            throw new IllegalStateException("Fine is not in PENDING status and cannot be waived. Current status: " + fine.getStatus());
        }

        fine.waiveFine();
        fine = fineRepository.save(fine);

        // Update UserProfile outstanding fines
        UserProfile userProfile = userProfileRepository.findByUserId(fine.getMemberId())
                .orElse(null);
        if (userProfile != null) {
            BigDecimal newOutstanding = userProfile.getOutstandingFines().subtract(fine.getAmount());
            if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                newOutstanding = BigDecimal.ZERO;
            }
            userProfile.setOutstandingFines(newOutstanding);
            userProfileRepository.save(userProfile);
        }

        log.info("Fine ID: {} waived successfully", fineId);
        return toFineResponse(fine);
    }

    @Transactional
    public FineResponse cancelFine(UUID fineId) {
        log.info("Cancelling fine ID: {}", fineId);
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with ID: " + fineId));

        if (!fine.isPending()) {
            throw new IllegalStateException("Fine is not in PENDING status and cannot be cancelled. Current status: " + fine.getStatus());
        }

        fine.cancelFine();
        fine = fineRepository.save(fine);

        // Update UserProfile outstanding fines
        UserProfile userProfile = userProfileRepository.findByUserId(fine.getMemberId())
                .orElse(null);
        if (userProfile != null) {
            BigDecimal newOutstanding = userProfile.getOutstandingFines().subtract(fine.getAmount());
            if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                newOutstanding = BigDecimal.ZERO;
            }
            userProfile.setOutstandingFines(newOutstanding);
            userProfileRepository.save(userProfile);
        }

        log.info("Fine ID: {} cancelled successfully", fineId);
        return toFineResponse(fine);
    }

    private FineResponse toFineResponse(Fine fine) {
        String memberName = userProfileRepository.findByUserId(fine.getMemberId())
                .map(UserProfile::getFullName)
                .orElse("Member #" + fine.getMemberId());
        return FineResponse.from(fine, memberName);
    }
}
