package org.systemhotelowy.dto;

import lombok.Getter;

@Getter
public class EmployeeKpiData {
    private long readyRooms;
    private long dirtyRooms;
    private long myTasks;
    private long myPendingTasks;

    public EmployeeKpiData(long readyRooms, long dirtyRooms, long myTasks, long myPendingTasks) {
        this.readyRooms = readyRooms;
        this.dirtyRooms = dirtyRooms;
        this.myTasks = myTasks;
        this.myPendingTasks = myPendingTasks;
    }

}
