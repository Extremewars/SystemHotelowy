package org.systemhotelowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.systemhotelowy.dto.TaskRequest;
import org.systemhotelowy.dto.TaskResponse;
import org.systemhotelowy.dto.UserResponse;
import org.systemhotelowy.dto.UserTaskCountResponse;
import org.systemhotelowy.mapper.TaskMapper;
import org.systemhotelowy.mapper.UserMapper;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Zarządzanie zadaniami")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RoomService roomService;

    public TaskController(TaskService taskService, TaskMapper taskMapper,
                          UserService userService, UserMapper userMapper,
                          RoomService roomService) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.userService = userService;
        this.userMapper = userMapper;
        this.roomService = roomService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Utwórz nowe zadanie",
            description = "Tworzy nowe zadanie. Walidacja: maksymalnie tyle tasków na dzień ile pokoi. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<?> create(@Valid @RequestBody TaskRequest request) {
        User assignedTo = null;
        User requestedBy = null;

        if (request.getAssignedToId() != null) {
            assignedTo = userService.findById(request.getAssignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + request.getAssignedToId() + " nie istnieje."));
        }

        // Jeśli nie podano requestedById, użyj aktualnie zalogowanego użytkownika
        if (request.getRequestedById() != null) {
            requestedBy = userService.findById(request.getRequestedById())
                    .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + request.getRequestedById() + " nie istnieje."));
        } else {
            // Automatycznie ustaw aktualnie zalogowanego użytkownika jako requestedBy
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                requestedBy = userService.findByEmail(userDetails.getUsername())
                        .orElse(null);
            }
        }

        Room room = roomService.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Pokój o ID " + request.getRoomId() + " nie istnieje."));

        Task task = taskMapper.toEntity(request, assignedTo, requestedBy, room);
        Task created = taskService.create(task);

        return ResponseEntity.created(URI.create("/api/tasks/" + created.getId()))
                .body(taskMapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie zadania", description = "Zwraca listę wszystkich zadań.")
    public List<TaskResponse> listAll() {
        return taskService.findAll().stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz zadanie po ID", description = "Zwraca szczegóły zadania o podanym ID.")
    public ResponseEntity<TaskResponse> getById(@PathVariable Integer id) {
        return taskService.findById(id)
                .map(task -> ResponseEntity.ok(taskMapper.toResponse(task)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Pobierz zadania dla pokoju", description = "Zwraca wszystkie zadania przypisane do danego pokoju.")
    public List<TaskResponse> getByRoomId(@PathVariable Integer roomId) {
        return taskService.findByRoomId(roomId).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/room/{roomId}/date/{date}")
    @Operation(summary = "Pobierz zadania dla pokoju i daty", description = "Zwraca zadania dla konkretnego pokoju i daty.")
    public List<TaskResponse> getByRoomIdAndDate(
            @PathVariable Integer roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return taskService.findByRoomIdAndDate(roomId, date).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Pobierz zadania dla daty", description = "Zwraca wszystkie zadania zaplanowane na konkretną datę.")
    public List<TaskResponse> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return taskService.findByDate(date).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Pobierz zadania użytkownika", description = "Zwraca wszystkie zadania przypisane do danego użytkownika.")
    public List<TaskResponse> getByUserId(@PathVariable Integer userId) {
        return taskService.findByAssignedToId(userId).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/count-per-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Kto ma ile tasków", description = "Zwraca liczbę zadań przypisanych do każdego użytkownika. Wymaga roli ADMIN lub MANAGER.")
    public List<UserTaskCountResponse> getTaskCountPerUser() {
        return taskService.getTaskCountPerUser();
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Pobierz listę użytkowników", description = "Zwraca listę wszystkich użytkowników. Dostępne tylko dla MANAGER.")
    public List<UserResponse> getUsersForManager() {
        return userService.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Aktualizuj zadanie", description = "Aktualizuje dane zadania. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody TaskRequest request) {
        try {
            User assignedTo = null;
            User requestedBy = null;

            if (request.getAssignedToId() != null) {
                assignedTo = userService.findById(request.getAssignedToId())
                        .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + request.getAssignedToId() + " nie istnieje."));
            }

            if (request.getRequestedById() != null) {
                requestedBy = userService.findById(request.getRequestedById())
                        .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + request.getRequestedById() + " nie istnieje."));
            }

            Room room = roomService.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Pokój o ID " + request.getRoomId() + " nie istnieje."));

            Task toUpdate = taskMapper.toEntity(request, assignedTo, requestedBy, room);
            toUpdate.setId(id);
            Task updated = taskService.update(toUpdate);

            return ResponseEntity.ok(taskMapper.toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Usuń zadanie", description = "Usuwa zadanie o podanym ID. Wymaga roli ADMIN lub MANAGER.")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            taskService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/can-create/{date}")
    @Operation(summary = "Sprawdź czy można utworzyć task na datę",
            description = "Sprawdza czy można utworzyć nowe zadanie na podaną datę (limit: ilość pokoi).")
    public ResponseEntity<Boolean> canCreateTaskForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskService.canCreateTaskForDate(date));
    }
}
