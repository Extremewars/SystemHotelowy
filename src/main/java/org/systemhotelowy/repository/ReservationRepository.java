package org.systemhotelowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.systemhotelowy.model.Reservation;
import org.systemhotelowy.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository dla operacji na rezerwacjach.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    /**
     * Znajduje wszystkie rezerwacje dla danego pokoju.
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE r.room.id = :roomId")
    List<Reservation> findByRoomId(@Param("roomId") Integer roomId);

    /**
     * Znajduje rezerwacje, które zaczynają się w podanym okresie.
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE r.checkInDate BETWEEN :startDate AND :endDate")
    List<Reservation> findByCheckInDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Znajduje rezerwacje, które kończą się w podanym okresie.
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE r.checkOutDate BETWEEN :startDate AND :endDate")
    List<Reservation> findByCheckOutDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Znajduje wszystkie rezerwacje w podanym okresie (check-in lub check-out).
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE " +
           "(r.checkInDate BETWEEN :startDate AND :endDate) OR " +
           "(r.checkOutDate BETWEEN :startDate AND :endDate) OR " +
           "(r.checkInDate <= :startDate AND r.checkOutDate >= :endDate)")
    List<Reservation> findReservationsInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Znajduje nakładające się rezerwacje dla danego pokoju w podanym okresie.
     * Używane do walidacji - czy można utworzyć nową rezerwację.
     */
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND " +
           "r.status NOT IN ('CANCELLED', 'CHECKED_OUT') AND " +
           "((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Integer roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    /**
     * Znajduje rezerwacje po statusie.
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE r.status = :status")
    List<Reservation> findByStatus(@Param("status") ReservationStatus status);

    /**
     * Znajduje aktywne rezerwacje (CONFIRMED lub CHECKED_IN).
     */
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.room WHERE r.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<Reservation> findActiveReservations();

    /**
     * Liczy rezerwacje dla pokoju w danym okresie.
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.room.id = :roomId AND " +
           "r.status NOT IN ('CANCELLED') AND " +
           "((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    long countReservationsForRoomInPeriod(
            @Param("roomId") Integer roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
