package org.systemhotelowy.service.impl;

import org.springframework.stereotype.Service;
import org.systemhotelowy.dto.EmployeeKpiData;
import org.systemhotelowy.dto.ManagerKpiData;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.service.DashboardService;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final RoomService roomService;
    private final TaskService taskService;

    public DashboardServiceImpl(RoomService roomService, TaskService taskService) {
        this.roomService = roomService;
        this.taskService = taskService;
    }

    @Override
    public ManagerKpiData getManagerKpiData() {
        long readyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.READY)
                .count();
        long dirtyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.DIRTY)
                .count();
        long outOfOrderRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.OUT_OF_ORDER)
                .count();
        long totalTasks = taskService.findAll().size();

        return new ManagerKpiData(readyRooms, dirtyRooms, outOfOrderRooms, totalTasks);
    }

    @Override
    public EmployeeKpiData getEmployeeKpiData(Integer userId) {
        long readyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.READY)
                .count();
        long dirtyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.DIRTY)
                .count();
        
        long myTasks = 0;
        long myPendingTasks = 0;
        
        if (userId != null) {
            var tasks = taskService.findByAssignedToId(userId);
            myTasks = tasks.size();
            myPendingTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
        }

        return new EmployeeKpiData(readyRooms, dirtyRooms, myTasks, myPendingTasks);
    }
}
