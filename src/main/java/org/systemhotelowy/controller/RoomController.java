package org.systemhotelowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.systemhotelowy.dto.RoomRequest;
import org.systemhotelowy.dto.RoomResponse;
import org.systemhotelowy.dto.RoomStatusRequest;
import org.systemhotelowy.mapper.RoomMapper;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.service.RoomService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Zarządzanie pokojami hotelowymi")
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    public RoomController(RoomService roomService, RoomMapper roomMapper) {
        this.roomService = roomService;
        this.roomMapper = roomMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Utwórz nowy pokój", description = "Tworzy nowy pokój hotelowy. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        Room created = roomService.create(roomMapper.toEntity(request));
        return ResponseEntity.created(URI.create("/api/rooms/" + created.getId()))
                .body(roomMapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie pokoje", description = "Zwraca listę wszystkich pokoi hotelowych.")
    public List<RoomResponse> listAll() {
        return roomService.findAll().stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz pokój po ID", description = "Zwraca szczegóły pokoju o podanym ID.")
    public ResponseEntity<RoomResponse> getById(@PathVariable Integer id) {
        return roomService.findById(id)
                .map(room -> ResponseEntity.ok(roomMapper.toResponse(room)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "Pobierz pokój po numerze", description = "Zwraca szczegóły pokoju o podanym numerze.")
    public ResponseEntity<RoomResponse> getByNumber(@PathVariable String number) {
        return roomService.findByNumber(number)
                .map(room -> ResponseEntity.ok(roomMapper.toResponse(room)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Aktualizuj pokój", description = "Aktualizuje dane pokoju. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<RoomResponse> update(@PathVariable Integer id, @Valid @RequestBody RoomRequest request) {
        Room toUpdate = roomMapper.toEntity(request);
        toUpdate.setId(id);
        Room updated = roomService.update(toUpdate);
        return ResponseEntity.ok(roomMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLEANER')")
    @Operation(summary = "Zmień status pokoju", description = "Aktualizuje status pokoju (np. DIRTY, CLEANING, READY). Dostępne dla ADMIN, MANAGER i CLEANER.")
    public ResponseEntity<RoomResponse> updateStatus(@PathVariable Integer id, @Valid @RequestBody RoomStatusRequest request) {
        Room updated = roomService.updateStatus(id, request.getRoomStatus());
        return ResponseEntity.ok(roomMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Usuń pokój", description = "Usuwa pokój o podanym ID. Wymaga roli ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
