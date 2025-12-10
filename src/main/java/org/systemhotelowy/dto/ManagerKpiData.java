package org.systemhotelowy.dto;

import lombok.Getter;

@Getter
public class ManagerKpiData {
    private long readyRooms;
    private long dirtyRooms;
    private long outOfOrderRooms;
    private long totalTasks;

    public ManagerKpiData(long readyRooms, long dirtyRooms, long outOfOrderRooms, long totalTasks) {
        this.readyRooms = readyRooms;
        this.dirtyRooms = dirtyRooms;
        this.outOfOrderRooms = outOfOrderRooms;
        this.totalTasks = totalTasks;
    }

}
