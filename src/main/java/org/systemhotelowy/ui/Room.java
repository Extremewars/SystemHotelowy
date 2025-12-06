package org.systemhotelowy.ui;
public  class Room {
    private String name;
    private int maxPeople;
    private double price;

    public Room(String name, int maxPeople, double price) {
        this.name = name;
        this.maxPeople = maxPeople;
        this.price = price;
    }

    public String getName() { return name; }
    public int getMaxPeople() { return maxPeople; }
    public double getPrice() { return price; }
}
