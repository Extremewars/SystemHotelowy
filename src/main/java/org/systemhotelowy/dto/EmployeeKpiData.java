package org.systemhotelowy.dto;

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

    public long getReadyRooms() { return readyRooms; }
    public long getDirtyRooms() { return dirtyRooms; }
    public long getMyTasks() { return myTasks; }
    public long getMyPendingTasks() { return myPendingTasks; }
}
