package org.systemhotelowy.dto;

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

    public long getReadyRooms() { return readyRooms; }
    public long getDirtyRooms() { return dirtyRooms; }
    public long getOutOfOrderRooms() { return outOfOrderRooms; }
    public long getTotalTasks() { return totalTasks; }
}
