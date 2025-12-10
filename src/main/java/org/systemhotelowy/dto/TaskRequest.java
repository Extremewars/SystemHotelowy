package org.systemhotelowy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.TaskStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "Opis zadania nie może być pusty")
    private String description;

    private String remarks;
    private TaskStatus status;

    @NotNull(message = "Data zaplanowania jest wymagana")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Czas trwania jest wymagany")
    @Min(value = 1, message = "Czas trwania musi wynosić co najmniej 1 minutę")
    private Integer durationInMinutes;

    private Integer assignedToId;
    private Integer requestedById;

    @NotNull(message = "ID pokoju jest wymagane")
    private Integer roomId;
}
