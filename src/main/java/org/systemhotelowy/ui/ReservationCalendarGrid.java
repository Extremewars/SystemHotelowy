package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class ReservationCalendarGrid extends VerticalLayout {

    private Grid<ReservationRow> reservationGrid;

    // Mock list of rooms
    private List<Room> rooms = new ArrayList<>();

    // Mock list of reservations
    private List<ReservationRow> reservations = new ArrayList<>();

    // Calendar pagination
    private LocalDate windowStart = LocalDate.now().minusDays(1); // yesterday
    private static final int WINDOW_SIZE = 14; // 14 days window

    public ReservationCalendarGrid() {
        setSpacing(true);
        setPadding(false);

        initMockData();

        reservationGrid = new Grid<>(ReservationRow.class, false);
        reservationGrid.setWidthFull();

        // --- PAGINATION BUTTONS ---
        Button prevBtn = new Button("◀", e -> {
            windowStart = windowStart.minusDays(7);
            refresh();
        });

        Button nextBtn = new Button("▶", e -> {
            windowStart = windowStart.plusDays(7);
            refresh();
        });

        HorizontalLayout pager = new HorizontalLayout(prevBtn, nextBtn);
        pager.setPadding(true);
        pager.setSpacing(true);

        // Add reservation button
        Button openFormButton = new Button("Dodaj rezerwację");
        openFormButton.addClickListener(e -> openReservationDialog());

        HorizontalLayout header = new HorizontalLayout(pager, openFormButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        refresh();
        add(header, reservationGrid);
    }

    private void openReservationDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField guestNameField = new TextField("Imię i nazwisko gościa");
        TextField phoneField = new TextField("Numer telefonu");

        ComboBox<Room> roomField = new ComboBox<>("Pokój");
        roomField.setItems(rooms);
        roomField.setItemLabelGenerator(Room::getName);

        DatePicker checkInField = new DatePicker("Check-in");
        DatePicker checkOutField = new DatePicker("Check-out");

        Button addReservationButton = new Button("Zapisz");
        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        addReservationButton.addClickListener(e -> {
            if (guestNameField.isEmpty() || phoneField.isEmpty() ||
                    roomField.getValue() == null || checkInField.isEmpty() || checkOutField.isEmpty()) {
                return;
            }

            Room selectedRoom = roomField.getValue();
            reservations.add(new ReservationRow(
                    selectedRoom.getName(),
                    selectedRoom.getMaxPeople(),
                    selectedRoom.getPrice(),
                    checkInField.getValue(),
                    checkOutField.getValue(),
                    guestNameField.getValue(),
                    phoneField.getValue()
            ));

            dialog.close();
            refresh();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(addReservationButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(guestNameField, phoneField, roomField,
                checkInField, checkOutField, buttonsLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void initMockData() {
        rooms.add(new Room("101", 2, 300));
        rooms.add(new Room("102", 3, 350));
        rooms.add(new Room("103", 4, 400));
        rooms.add(new Room("104", 2, 280));

        LocalDate now = LocalDate.now();

        reservations.add(new ReservationRow("101", 2, 300,
                now.plusDays(1), now.plusDays(3),
                "Jan Kowalski", "123456789"));

        reservations.add(new ReservationRow("103", 4, 400,
                now.plusDays(5), now.plusDays(6),
                "Anna Nowak", "987654321"));
    }

    private void refresh() {
        reservationGrid.removeAllColumns();

        // FIXED columns:
        reservationGrid.addColumn(ReservationRow::getRoom).setHeader("Pokój").setAutoWidth(true);
        reservationGrid.addColumn(ReservationRow::getMaxPeople).setHeader("Max osób").setAutoWidth(true);
        reservationGrid.addColumn(ReservationRow::getPrice).setHeader("Cena/doba").setAutoWidth(true);

        // Calendar columns
        for (int i = 0; i < WINDOW_SIZE; i++) {
            LocalDate currentDay = windowStart.plusDays(i);

            String dayLabel = currentDay.getDayOfMonth() +
                    " (" + currentDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pl")) + ")";

            reservationGrid.addComponentColumn(row -> {
                boolean booked = reservations.stream().anyMatch(r ->
                        r.getRoom().equals(row.getRoom()) &&
                                r.getCheckIn() != null &&
                                r.getCheckOut() != null &&
                                !r.getCheckIn().isAfter(currentDay) &&
                                !r.getCheckOut().isBefore(currentDay)
                );

                Span cell = new Span(booked ? "X" : "");
                cell.getStyle()
                        .set("display", "block")
                        .set("text-align", "center")
                        .set("background-color", booked ? "lightblue" : "transparent")
                        .set("border", "1px solid #ddd")
                        .set("padding", "4px");
                return cell;

            }).setHeader(dayLabel);
        }

        // Generate rows: one per room
        List<ReservationRow> rows = new ArrayList<>();
        for (Room r : rooms) {
            rows.add(new ReservationRow(r.getName(), r.getMaxPeople(), r.getPrice(),
                    null, null, null, null));
        }

        reservationGrid.setItems(rows);
    }


    // -------------------------------------------------------
    // ROOM MODEL
    // -------------------------------------------------------
    public static class Room {
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


    // -------------------------------------------------------
    // RESERVATION MODEL
    // -------------------------------------------------------
    public static class ReservationRow {
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
    }
}
