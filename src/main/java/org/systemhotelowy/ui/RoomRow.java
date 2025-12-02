package org.systemhotelowy.ui;

public class RoomRow {
    private String room;
    private String status;
    private String worker;
    private String tasks;
    private String notes;

    public RoomRow(String room, String status, String worker, String tasks, String notes) {
        this.room = room;
        this.status = status;
        this.worker = worker;
        this.tasks = tasks;
        this.notes = notes;
    }

    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public String getWorker() { return worker; }
    public String getTasks() { return tasks; }
    public String getNotes() { return notes; }
}
