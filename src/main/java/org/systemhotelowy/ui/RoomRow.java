package org.systemhotelowy.ui;

import java.util.ArrayList;
import java.util.List;

public class RoomRow {
    private String room;
    private String status;
    private String worker;
    private List<Task> tasks;
    private String notes;

    public RoomRow(String room, String status, String worker, String notes, String s) {
        this.room = room;
        this.status = status;
        this.worker = worker;
        this.tasks = new ArrayList<>();
        this.notes = notes;
    }

    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public String getWorker() { return worker; }
    public List<Task> getTasks() { return tasks; }
    public String getNotes() { return notes; }

    public void addTask(Task task) { tasks.add(task); }
    public void removeTask(Task task) { tasks.remove(task); }
}
