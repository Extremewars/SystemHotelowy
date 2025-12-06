package org.systemhotelowy.ui.ManagerDashboard;

import java.time.LocalDate;

public class ReservationRow {
    private String room;
    private int maxPeople;
    private double price;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String guestName;
    private String phone;

    public ReservationRow(String room, int maxPeople, double price,
                          LocalDate checkIn, LocalDate checkOut,
                          String guestName, String phone) {
        this.room = room;
        this.maxPeople = maxPeople;
        this.price = price;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestName = guestName;
        this.phone = phone;
    }

    public String getRoom() { return room; }
    public int getMaxPeople() { return maxPeople; }
    public double getPrice() { return price; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }

    public void setCheckIn(LocalDate d) { this.checkIn = d; }
    public void setCheckOut(LocalDate d) { this.checkOut = d; }
    public void setGuestName(String n) { this.guestName = n; }
    public void setPhone(String p) { this.phone = p; }
}