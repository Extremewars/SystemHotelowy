package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class ReservationCalendarGrid extends VerticalLayout {

    private Grid<ReservationRow> reservationGrid;

    private List<Room> rooms = new ArrayList<>();
    private List<ReservationRow> reservations = new ArrayList<>();

    private LocalDate windowStart = LocalDate.now().minusDays(1);
    private int WINDOW_SIZE = 14;

    public ReservationCalendarGrid() {
        setSpacing(true);
        setPadding(true);

        initMockData();

        reservationGrid = new Grid<>(ReservationRow.class, false);
        reservationGrid.setWidthFull();
        reservationGrid.getStyle()
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        Button prevBtn = new Button("◀", e -> {
            windowStart = windowStart.minusDays(WINDOW_SIZE);
            refresh();
        });

        Button nextBtn = new Button("▶", e -> {
            windowStart = windowStart.plusDays(WINDOW_SIZE);
            refresh();
        });

        HorizontalLayout pager = new HorizontalLayout(prevBtn, nextBtn);

        Button openFormButton = new Button("Dodaj rezerwację");
        openFormButton.addClickListener(e -> openReservationDialog(null, null));

        Button zoom7 = new Button("7 dni", e -> { WINDOW_SIZE = 7; refresh(); });
        Button zoom14 = new Button("14 dni", e -> { WINDOW_SIZE = 14; refresh(); });
        Button zoom30 = new Button("30 dni", e -> { WINDOW_SIZE = 30; refresh(); });

        HorizontalLayout zoom = new HorizontalLayout(zoom7, zoom14, zoom30);

        HorizontalLayout header = new HorizontalLayout(pager, zoom, openFormButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        refresh();
        add(header, reservationGrid);
    }

    // ------------------------------------------------------------------------
    // CELL CLICK HANDLING
    // ------------------------------------------------------------------------

    private void handleCellClick(Room room, LocalDate day) {
        Optional<ReservationRow> found = reservations.stream()
                .filter(r ->
                        r.getRoom().equals(room.getName()) &&
                                !r.getCheckIn().isAfter(day) &&
                                !r.getCheckOut().isBefore(day)
                )
                .findFirst();

        if (found.isPresent()) {
            openReservationEditDialog(found.get());
        } else {
            openReservationDialog(room, day);
        }
    }


    // ------------------------------------------------------------------------
    // ADD RESERVATION DIALOG
    // ------------------------------------------------------------------------

    private void openReservationDialog(Room preselectedRoom, LocalDate preselectedDay) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField guestNameField = new TextField("Imię i nazwisko gościa");
        TextField phoneField = new TextField("Numer telefonu");

        ComboBox<Room> roomField = new ComboBox<>("Pokój");
        roomField.setItems(rooms);
        roomField.setItemLabelGenerator(Room::getName);

        DatePicker checkInField = new DatePicker("Check-in");
        DatePicker checkOutField = new DatePicker("Check-out");

        if (preselectedRoom != null) roomField.setValue(preselectedRoom);
        if (preselectedDay != null) {
            checkInField.setValue(preselectedDay);
            checkOutField.setValue(preselectedDay.plusDays(1));
        }

        Button addBtn = new Button("Zapisz", e -> {
            if (roomField.isEmpty() || checkInField.isEmpty() || checkOutField.isEmpty()) return;

            reservations.add(new ReservationRow(
                    roomField.getValue().getName(),
                    roomField.getValue().getMaxPeople(),
                    roomField.getValue().getPrice(),
                    checkInField.getValue(),
                    checkOutField.getValue(),
                    guestNameField.getValue(),
                    phoneField.getValue()
            ));

            dialog.close();
            refresh();
        });

        dialog.add(new VerticalLayout(
                guestNameField, phoneField, roomField, checkInField, checkOutField,
                new HorizontalLayout(addBtn, new Button("Anuluj", e -> dialog.close()))
        ));

        dialog.open();
    }


    // ------------------------------------------------------------------------
    // EDIT RESERVATION DIALOG
    // ------------------------------------------------------------------------

    private void openReservationEditDialog(ReservationRow reservation) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField guestNameField = new TextField("Imię i nazwisko gościa", reservation.getGuestName());
        TextField phoneField = new TextField("Numer telefonu", reservation.getPhone());

        DatePicker checkInField = new DatePicker("Check-in", reservation.getCheckIn());
        DatePicker checkOutField = new DatePicker("Check-out", reservation.getCheckOut());

        Button editBtn = new Button("Zapisz zmiany", e -> {
            reservation.setGuestName(guestNameField.getValue());
            reservation.setPhone(phoneField.getValue());
            reservation.setCheckIn(checkInField.getValue());
            reservation.setCheckOut(checkOutField.getValue());
            dialog.close();
            refresh();
        });

        dialog.add(new VerticalLayout(
                guestNameField, phoneField, checkInField, checkOutField,
                new HorizontalLayout(editBtn, new Button("Anuluj", e -> dialog.close()))
        ));

        dialog.open();
    }


    // ------------------------------------------------------------------------
    // MOCK DATA
    // ------------------------------------------------------------------------

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


    // ------------------------------------------------------------------------
    // REFRESH GRID
    // ------------------------------------------------------------------------

    private void refresh() {
        reservationGrid.removeAllColumns();

        reservationGrid.addColumn(ReservationRow::getRoom)
                .setHeader("Pokój")
                .setWidth("70px")
                .getStyle().set("font-weight", "600").set("background-color", "#fafafa");

        reservationGrid.addColumn(ReservationRow::getMaxPeople)
                .setHeader("Osoby")
                .setWidth("60px");

        reservationGrid.addColumn(ReservationRow::getPrice)
                .setHeader("Cena")
                .setWidth("70px");

        for (int i = 0; i < WINDOW_SIZE; i++) {
            LocalDate currentDay = windowStart.plusDays(i);

            String dow = currentDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pl"));
            String headerHtml = "<div style='text-align:center;font-size:12px'><b>" +
                    dow + "</b><br/>" + currentDay.getDayOfMonth() + "</div>";

            Span header = new Span();
            header.getElement().setProperty("innerHTML", headerHtml);

            reservationGrid.addComponentColumn(row -> createCell(row, currentDay))
                    .setHeader(header)
                    .setWidth("60px")
                    .setFlexGrow(0);
        }

        List<ReservationRow> rows = new ArrayList<>();
        for (Room r : rooms)
            rows.add(new ReservationRow(r.getName(), r.getMaxPeople(), r.getPrice(), null, null, null, null));

        reservationGrid.setItems(rows);
    }


    // ------------------------------------------------------------------------
    // CELL RENDERING (COLOR BARS, ROUNDED CORNERS, HOVER)
    // ------------------------------------------------------------------------

    private Div createCell(ReservationRow row, LocalDate day) {
        Div cell = new Div();
        cell.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("padding", "6px")
                .set("border-radius", "4px")
                .set("transition", "background-color 0.15s")
                .set("cursor", "pointer")
                .set("height", "26px");

        Room room = rooms.stream()
                .filter(r -> r.getName().equals(row.getRoom()))
                .findFirst().orElse(null);

        Optional<ReservationRow> res = reservations.stream()
                .filter(r ->
                        r.getRoom().equals(row.getRoom()) &&
                                !r.getCheckIn().isAfter(day) &&
                                !r.getCheckOut().isBefore(day)
                )
                .findFirst();

        if (res.isPresent()) {
            ReservationRow rez = res.get();

            String color = getColorForGuest(rez.getGuestName());
            cell.getStyle().set("background-color", color);

            if (day.equals(rez.getCheckIn()))
                cell.getStyle().set("border-top-left-radius", "12px").set("border-bottom-left-radius", "12px");

            if (day.equals(rez.getCheckOut()))
                cell.getStyle().set("border-top-right-radius", "12px").set("border-bottom-right-radius", "12px");

            cell.getElement().setProperty("title",
                    rez.getGuestName() + " (" + rez.getPhone() + ")");

        } else {
            cell.getElement().addEventListener("mouseover", e ->
                    cell.getStyle().set("background-color", "#f2f2f2"));
            cell.getElement().addEventListener("mouseout", e ->
                    cell.getStyle().set("background-color", "transparent"));
        }

        LocalDate finalDay = day;
        Room finalRoom = room;

        cell.addClickListener(e -> handleCellClick(finalRoom, finalDay));

        return cell;
    }


    // ------------------------------------------------------------------------
    // GENEROWANIE KOLORÓW
    // ------------------------------------------------------------------------

    private String getColorForGuest(String guestName) {
        int hash = Math.abs(guestName.hashCode());
        int r = (hash % 128) + 100;
        int g = ((hash / 128) % 128) + 100;
        int b = ((hash / (128 * 128)) % 128) + 100;
        return "rgb(" + r + "," + g + "," + b + ")";
    }


    // ===========================================================
    // MODEL
    // ===========================================================

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

        public void setCheckIn(LocalDate d) { this.checkIn = d; }
        public void setCheckOut(LocalDate d) { this.checkOut = d; }
        public void setGuestName(String n) { this.guestName = n; }
        public void setPhone(String p) { this.phone = p; }
    }
}
