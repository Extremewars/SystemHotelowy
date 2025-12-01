package org.systemhotelowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.systemhotelowy.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByRoomId(Integer roomId);

    List<Task> findByAssignedToId(Integer userId);

    @Query("SELECT t FROM Task t WHERE t.room.id = :roomId AND DATE(t.scheduledAt) = :date")
    List<Task> findByRoomIdAndDate(@Param("roomId") Integer roomId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE DATE(t.scheduledAt) = :date")
    List<Task> findByDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.scheduledAt BETWEEN :start AND :end")
    List<Task> findByScheduledAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Task t WHERE DATE(t.scheduledAt) = :date")
    long countTasksByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId")
    long countByAssignedToId(@Param("userId") Integer userId);

    @Query("SELECT t.assignedTo.id, COUNT(t) FROM Task t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo.id")
    List<Object[]> countTasksGroupedByUser();
}
