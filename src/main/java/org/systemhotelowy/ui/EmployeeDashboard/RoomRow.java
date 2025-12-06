package org.systemhotelowy.ui.EmployeeDashboard;

import org.systemhotelowy.ui.Report;
import org.systemhotelowy.ui.Task;

import java.util.ArrayList;
import java.util.List;

public class RoomRow {

    private String room;
    private String status;
    private String location;
    private String equipment;
    private String maxPeople;

    private List<Task> tasks;
    private List<Report> reports;

    public RoomRow(String room, String status, String location, String equipment, String maxPeople) {
        this.room = room;
        this.status = status;
        this.location = location;
        this.equipment = equipment;
        this.maxPeople = maxPeople;
        this.tasks = new ArrayList<>();
        this.reports = new ArrayList<>();
    }

    // ===== Gettery =====
    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public String getLocation() { return location; }
    public String getEquipment() { return equipment; }
    public String getMaxPeople() { return maxPeople; }

    public List<Task> getTasks() { return tasks; }
    public List<Report> getReports() { return reports; }

    // ===== Zadania =====
    public void addTask(Task task) { tasks.add(task); }
    public void removeTask(Task task) { tasks.remove(task); }

    // ===== Zgłoszenia =====
    public void addReport(Report report) { reports.add(report); }
    public void removeReport(Report report) { reports.remove(report); }
}
