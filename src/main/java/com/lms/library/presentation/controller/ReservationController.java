package com.lms.library.presentation.controller;

import com.lms.library.application.dto.CreateReservationRequest;
import com.lms.library.application.dto.ReservationResponse;
import com.lms.library.application.service.ReservationService;
import com.lms.library.application.service.AuthenticationService;
import com.lms.library.domain.entity.Reservation;
import com.lms.library.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody CreateReservationRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = authenticationService.findByEmail(email);
        Long userId = user.getId();
        ReservationResponse response = reservationService.createReservation(
                userId, request.getBookId(), request.getPriority(), request.getNotes());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable UUID id,
            Authentication authentication) {
        String email = authentication.getName();
        User user = authenticationService.findByEmail(email);
        Long userId = user.getId();
        reservationService.cancelReservation(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-reservations")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        String email = authentication.getName();
        User user = authenticationService.findByEmail(email);
        Long userId = user.getId();
        List<ReservationResponse> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getBookReservations(@PathVariable UUID bookId) {
        List<ReservationResponse> reservations = reservationService.getBookReservations(bookId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) Reservation.ReservationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReservationResponse> reservations = reservationService.getAllReservations(status, pageable);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/{id}/fulfill")
    @PreAuthorize("hasAnyAuthority('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> fulfillReservation(@PathVariable UUID id) {
        reservationService.fulfillReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable UUID id,
            Authentication authentication) {
        String email = authentication.getName();
        User user = authenticationService.findByEmail(email);
        Long userId = user.getId();
        ReservationResponse response = reservationService.confirmBorrowFromHold(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/book/{bookId}/count")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Long> getReservationCount(@PathVariable UUID bookId) {
        long count = reservationService.getReservationCount(bookId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ReservationResponse> checkReservationStatus(
            @RequestParam UUID bookId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = authenticationService.findByEmail(email);
        Long userId = user.getId();
        log.info("Checking reservation status for user: {} and book: {}", userId, bookId);
        ReservationResponse response = reservationService.checkReservationStatus(userId, bookId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
