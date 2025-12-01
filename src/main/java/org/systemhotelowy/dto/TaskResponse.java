package org.systemhotelowy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.TaskStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Integer id;
    private String description;
    private String remarks;
    private TaskStatus status;
    private LocalDateTime scheduledAt;
    private Integer durationInMinutes;
    private UserResponse assignedTo;
    private UserResponse requestedBy;
    private RoomResponse room;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
