package org.systemhotelowy.service;

import org.systemhotelowy.dto.UserTaskCountResponse;
import org.systemhotelowy.model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task create(Task task);
    List<Task> createBatch(List<Task> tasks);
    Optional<Task> findById(Integer id);
    List<Task> findAll();
    List<Task> findByRoomId(Integer roomId);
    List<Task> findByRoomIdAndDate(Integer roomId, LocalDate date);
    List<Task> findByDate(LocalDate date);
    List<Task> findByAssignedToId(Integer userId);
    Task update(Task task);
    void deleteById(Integer id);
    List<UserTaskCountResponse> getTaskCountPerUser();
    boolean canCreateTaskForDate(LocalDate date);
    boolean canCreateTasksForRoomsAndDate(List<Integer> roomIds, LocalDate date);
}
