package org.systemhotelowy.service;

import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.dto.ReservationResponse;
import org.systemhotelowy.model.Reservation;
import org.systemhotelowy.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfejs serwisu do zarządzania rezerwacjami.
 */
public interface ReservationService {

    /**
     * Znajduje wszystkie rezerwacje.
     */
    List<Reservation> findAll();

    /**
     * Znajduje rezerwację po ID.
     */
    Reservation findById(Integer id);

    /**
     * Znajduje rezerwacje dla danego pokoju.
     */
    List<Reservation> findByRoomId(Integer roomId);

    /**
     * Znajduje rezerwacje w podanym okresie.
     */
    List<Reservation> findReservationsInPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Znajduje rezerwacje po statusie.
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Znajduje aktywne rezerwacje (CONFIRMED lub CHECKED_IN).
     */
    List<Reservation> findActiveReservations();

    /**
     * Tworzy nową rezerwację.
     * Waliduje czy pokój jest dostępny w podanym okresie.
     */
    Reservation create(ReservationRequest request);

    /**
     * Aktualizuje istniejącą rezerwację.
     */
    Reservation update(Integer id, ReservationRequest request);

    /**
     * Usuwa rezerwację.
     */
    void delete(Integer id);

    /**
     * Zmienia status rezerwacji.
     */
    Reservation changeStatus(Integer id, ReservationStatus newStatus);

    /**
     * Sprawdza czy pokój jest dostępny w podanym okresie.
     */
    boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * Sprawdza czy pokój jest dostępny w podanym okresie, z wyłączeniem podanej rezerwacji.
     */
    boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate, Integer excludeReservationId);

    /**
     * Konwertuje Reservation na ReservationResponse.
     */
    ReservationResponse toResponse(Reservation reservation);

    /**
     * Konwertuje listę Reservation na listę ReservationResponse.
     */
    List<ReservationResponse> toResponseList(List<Reservation> reservations);
}
