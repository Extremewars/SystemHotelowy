package org.systemhotelowy.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.dto.ReservationResponse;
import org.systemhotelowy.mapper.ReservationMapper;
import org.systemhotelowy.model.Reservation;
import org.systemhotelowy.model.ReservationStatus;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.repository.ReservationRepository;
import org.systemhotelowy.repository.RoomRepository;
import org.systemhotelowy.service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacja serwisu do zarządzania rezerwacjami.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findById(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rezerwacja nie znaleziona: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByRoomId(Integer roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsInPeriod(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findReservationsInPeriod(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findActiveReservations() {
        return reservationRepository.findActiveReservations();
    }

    @Override
    public Reservation create(ReservationRequest request) {
        // Walidacja dat
        if (request.getCheckInDate().isAfter(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Data wymeldowania musi być późniejsza niż data zameldowania");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data zameldowania nie może być w przeszłości");
        }

        // Sprawdź czy pokój istnieje
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Pokój nie znaleziony: " + request.getRoomId()));

        // Sprawdź czy pokój jest dostępny
        if (!isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new IllegalArgumentException("Pokój jest już zarezerwowany w tym okresie");
        }

        // Utwórz rezerwację
        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setRoom(room);

        if (reservation.getStatus() == null) {
            reservation.setStatus(ReservationStatus.PENDING);
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation update(Integer id, ReservationRequest request) {
        Reservation existing = findById(id);

        // Walidacja dat
        if (request.getCheckInDate().isAfter(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Data wymeldowania musi być późniejsza niż data zameldowania");
        }

        // Jeśli zmieniono pokój lub daty, sprawdź dostępność
        boolean roomChanged = !existing.getRoom().getId().equals(request.getRoomId());
        boolean datesChanged = !existing.getCheckInDate().equals(request.getCheckInDate()) ||
                               !existing.getCheckOutDate().equals(request.getCheckOutDate());

        if (roomChanged || datesChanged) {
            if (!isRoomAvailable(request.getRoomId(), request.getCheckInDate(), 
                               request.getCheckOutDate(), id)) {
                throw new IllegalArgumentException("Pokój jest już zarezerwowany w tym okresie");
            }
        }

        // Jeśli zmieniono pokój, zaktualizuj referencję
        if (roomChanged) {
            Room newRoom = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Pokój nie znaleziony: " + request.getRoomId()));

            existing.setRoom(newRoom);
        }

        // Aktualizuj pozostałe pola
        reservationMapper.updateEntityFromRequest(request, existing);

        return reservationRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        Reservation reservation = findById(id);
        reservationRepository.delete(reservation);
    }

    @Override
    public Reservation changeStatus(Integer id, ReservationStatus newStatus) {
        Reservation reservation = findById(id);
        reservation.setStatus(newStatus);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return isRoomAvailable(roomId, checkInDate, checkOutDate, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate, 
                                  Integer excludeReservationId) {
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                roomId, checkInDate, checkOutDate);

        if (excludeReservationId != null) {
            overlapping = overlapping.stream()
                    .filter(r -> !r.getId().equals(excludeReservationId))
                    .collect(Collectors.toList());
        }

        return overlapping.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse toResponse(Reservation reservation) {
        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> toResponseList(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
