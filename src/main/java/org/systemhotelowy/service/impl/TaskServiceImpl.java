package org.systemhotelowy.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.systemhotelowy.dto.UserTaskCountResponse;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.User;
import org.systemhotelowy.repository.RoomRepository;
import org.systemhotelowy.repository.TaskRepository;
import org.systemhotelowy.repository.UserRepository;
import org.systemhotelowy.service.TaskService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Task create(Task task) {
        LocalDate taskDate = task.getScheduledAt().toLocalDate();
        if (!canCreateTaskForDate(taskDate)) {
            throw new IllegalStateException(
                    "Nie można utworzyć zadania - osiągnięto limit tasków na dzień " + taskDate + 
                    " (maksymalnie tyle tasków ile pokoi)."
            );
        }
        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByRoomId(Integer roomId) {
        return taskRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByRoomIdAndDate(Integer roomId, LocalDate date) {
        return taskRepository.findByRoomIdAndDate(roomId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByDate(LocalDate date) {
        return taskRepository.findByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByAssignedToId(Integer userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    @Override
    public Task update(Task task) {
        Integer id = task.getId();
        if (id == null) {
            throw new IllegalArgumentException("Task id must not be null for update.");
        }
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Task with id " + id + " not found."));

        existing.setDescription(task.getDescription());
        existing.setRemarks(task.getRemarks());
        existing.setStatus(task.getStatus());
        existing.setScheduledAt(task.getScheduledAt());
        existing.setDurationInMinutes(task.getDurationInMinutes());
        existing.setAssignedTo(task.getAssignedTo());
        existing.setRequestedBy(task.getRequestedBy());
        existing.setRoom(task.getRoom());

        return taskRepository.save(existing);
    }

    @Override
    public void deleteById(Integer id) {
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTaskCountResponse> getTaskCountPerUser() {
        List<Object[]> results = taskRepository.countTasksGroupedByUser();
        List<UserTaskCountResponse> responses = new ArrayList<>();

        for (Object[] result : results) {
            Integer userId = (Integer) result[0];
            Long count = (Long) result[1];

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                responses.add(new UserTaskCountResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        count
                ));
            }
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateTaskForDate(LocalDate date) {
        long roomCount = roomRepository.count();
        long taskCountForDate = taskRepository.countTasksByDate(date);
        return taskCountForDate < roomCount;
    }
}
