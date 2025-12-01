package org.systemhotelowy.mapper;

import org.springframework.stereotype.Component;
import org.systemhotelowy.dto.TaskRequest;
import org.systemhotelowy.dto.TaskResponse;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.model.User;

@Component
public class TaskMapper {

    private final UserMapper userMapper;
    private final RoomMapper roomMapper;

    public TaskMapper(UserMapper userMapper, RoomMapper roomMapper) {
        this.userMapper = userMapper;
        this.roomMapper = roomMapper;
    }

    public Task toEntity(TaskRequest request, User assignedTo, User requestedBy, Room room) {
        if (request == null) {
            return null;
        }

        Task task = new Task();
        task.setDescription(request.getDescription());
        task.setRemarks(request.getRemarks());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING);
        task.setScheduledAt(request.getScheduledAt());
        task.setDurationInMinutes(request.getDurationInMinutes());
        task.setAssignedTo(assignedTo);
        task.setRequestedBy(requestedBy);
        task.setRoom(room);

        return task;
    }

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setDescription(task.getDescription());
        response.setRemarks(task.getRemarks());
        response.setStatus(task.getStatus());
        response.setScheduledAt(task.getScheduledAt());
        response.setDurationInMinutes(task.getDurationInMinutes());
        response.setAssignedTo(userMapper.toResponse(task.getAssignedTo()));
        response.setRequestedBy(userMapper.toResponse(task.getRequestedBy()));
        response.setRoom(roomMapper.toResponse(task.getRoom()));
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        return response;
    }
}
