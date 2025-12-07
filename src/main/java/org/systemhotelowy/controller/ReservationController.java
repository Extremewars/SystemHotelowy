package org.systemhotelowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.dto.ReservationResponse;
import org.systemhotelowy.model.ReservationStatus;
import org.systemhotelowy.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Kontroler REST API dla zarządzania rezerwacjami.
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Zarządzanie rezerwacjami hotelowymi")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Utwórz nową rezerwację", description = "Tworzy nową rezerwację pokoju. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        var created = reservationService.create(request);
        return ResponseEntity.created(URI.create("/api/reservations/" + created.getId()))
                .body(reservationService.toResponse(created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz wszystkie rezerwacje", description = "Zwraca listę wszystkich rezerwacji. Wymaga roli ADMIN lub MANAGER.")
    public List<ReservationResponse> listAll() {
        return reservationService.toResponseList(reservationService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz rezerwację po ID", description = "Zwraca szczegóły rezerwacji o podanym ID.")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Integer id) {
        try {
            var reservation = reservationService.findById(id);
            return ResponseEntity.ok(reservationService.toResponse(reservation));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz rezerwacje dla pokoju", description = "Zwraca wszystkie rezerwacje dla danego pokoju.")
    public List<ReservationResponse> getByRoomId(@PathVariable Integer roomId) {
        return reservationService.toResponseList(reservationService.findByRoomId(roomId));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz rezerwacje w okresie", description = "Zwraca rezerwacje w podanym okresie czasu.")
    public List<ReservationResponse> getByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reservationService.toResponseList(
                reservationService.findReservationsInPeriod(startDate, endDate)
        );
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz rezerwacje po statusie", description = "Zwraca rezerwacje o podanym statusie (PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED).")
    public List<ReservationResponse> getByStatus(@PathVariable ReservationStatus status) {
        return reservationService.toResponseList(reservationService.findByStatus(status));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Pobierz aktywne rezerwacje", description = "Zwraca wszystkie aktywne rezerwacje (CONFIRMED lub CHECKED_IN).")
    public List<ReservationResponse> getActive() {
        return reservationService.toResponseList(reservationService.findActiveReservations());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Aktualizuj rezerwację", description = "Aktualizuje dane rezerwacji. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ReservationRequest request
    ) {
        try {
            var updated = reservationService.update(id, request);
            return ResponseEntity.ok(reservationService.toResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Zmień status rezerwacji", description = "Aktualizuje status rezerwacji (PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED).")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Integer id,
            @RequestParam ReservationStatus status
    ) {
        try {
            var updated = reservationService.changeStatus(id, status);
            return ResponseEntity.ok(reservationService.toResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Usuń rezerwację", description = "Usuwa rezerwację z systemu. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            reservationService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Sprawdź dostępność pokoju", description = "Sprawdza czy pokój jest dostępny w podanym okresie.")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Integer roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) Integer excludeReservationId
    ) {
        boolean available;
        if (excludeReservationId != null) {
            available = reservationService.isRoomAvailable(roomId, checkInDate, checkOutDate, excludeReservationId);
        } else {
            available = reservationService.isRoomAvailable(roomId, checkInDate, checkOutDate);
        }
        return ResponseEntity.ok(available);
    }
}
