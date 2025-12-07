package org.systemhotelowy.mapper;

import org.springframework.stereotype.Component;
import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.dto.ReservationResponse;
import org.systemhotelowy.model.Reservation;

/**
 * Mapper dla konwersji między Reservation a DTO (bez MapStruct).
 */
@Component
public class ReservationMapper {

    /**
     * Konwertuje ReservationRequest na encję Reservation.
     * Room musi być ustawiony ręcznie w serwisie.
     */
    public Reservation toEntity(ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setGuestName(request.getGuestName());
        reservation.setGuestEmail(request.getGuestEmail());
        reservation.setGuestPhone(request.getGuestPhone());
        reservation.setNumberOfGuests(request.getNumberOfGuests());
        reservation.setTotalPrice(request.getTotalPrice());
        reservation.setStatus(request.getStatus());
        reservation.setNotes(request.getNotes());
        return reservation;
    }

    /**
     * Konwertuje encję Reservation na ReservationResponse.
     */
    public ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setRoomId(reservation.getRoom().getId());
        response.setRoomNumber(reservation.getRoom().getNumber());
        response.setCheckInDate(reservation.getCheckInDate());
        response.setCheckOutDate(reservation.getCheckOutDate());
        response.setGuestName(reservation.getGuestName());
        response.setGuestEmail(reservation.getGuestEmail());
        response.setGuestPhone(reservation.getGuestPhone());
        response.setNumberOfGuests(reservation.getNumberOfGuests());
        response.setTotalPrice(reservation.getTotalPrice());
        response.setStatus(reservation.getStatus());
        response.setNotes(reservation.getNotes());
        response.setCreatedAt(reservation.getCreatedAt());
        return response;
    }

    /**
     * Aktualizuje istniejącą encję Reservation na podstawie ReservationRequest.
     * Room musi być zaktualizowany ręcznie w serwisie jeśli się zmienił.
     */
    public void updateEntityFromRequest(ReservationRequest request, Reservation reservation) {
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setGuestName(request.getGuestName());
        reservation.setGuestEmail(request.getGuestEmail());
        reservation.setGuestPhone(request.getGuestPhone());
        reservation.setNumberOfGuests(request.getNumberOfGuests());
        reservation.setTotalPrice(request.getTotalPrice());
        reservation.setStatus(request.getStatus());
        reservation.setNotes(request.getNotes());
    }
}
