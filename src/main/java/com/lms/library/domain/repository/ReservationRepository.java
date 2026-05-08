package com.lms.library.domain.repository;

import com.lms.library.domain.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByBookId(UUID bookId);

    Optional<Reservation> findByUserIdAndBookIdAndStatus(
            Long userId, UUID bookId, Reservation.ReservationStatus status);

    List<Reservation> findByBookIdAndStatusOrderByPriorityDescCreatedAtAsc(
            UUID bookId, Reservation.ReservationStatus status);

    long countByBookIdAndStatus(UUID bookId, Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ON_HOLD' AND r.holdExpiresAt < :now")
    List<Reservation> findExpiredHolds(LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.bookId = :bookId ORDER BY r.priority DESC, r.createdAt ASC")
    List<Reservation> findActiveReservationsByBookIdOrdered(UUID bookId);

    Page<Reservation> findByStatus(Reservation.ReservationStatus status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' ORDER BY r.priority DESC, r.createdAt ASC")
    Page<Reservation> findActiveReservationsOrdered(Pageable pageable);
}
