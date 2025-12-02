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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ReservationCalendarGrid extends VerticalLayout {

    private Grid<ReservationRow> reservationGrid;
    private ComboBox<YearMonth> monthPicker;

    private List<String> rooms = new ArrayList<>();
    private List<ReservationRow> reservations = new ArrayList<>();

    public ReservationCalendarGrid() {
        setSpacing(true);
        setPadding(false);

        initMockData();

        // Picker miesiąca
        monthPicker = new ComboBox<>("Wybierz miesiąc");
        YearMonth now = YearMonth.now();
        monthPicker.setItems(now, now.plusMonths(1), now.plusMonths(2));
        monthPicker.setValue(now);
        monthPicker.addValueChangeListener(e -> refresh());

        // Grid rezerwacji
        reservationGrid = new Grid<>(ReservationRow.class, false);
        reservationGrid.setWidthFull();

        // Przycisk do otwierania formularza w dialogu
        Button openFormButton = new Button("Dodaj rezerwację");
        openFormButton.addClickListener(e -> openReservationDialog());

        HorizontalLayout header = new HorizontalLayout(monthPicker, openFormButton);
        header.setWidthFull();

        refresh();
        add(header, reservationGrid);
    }

    private void openReservationDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField guestNameField = new TextField("Imię i nazwisko gościa");
        TextField phoneField = new TextField("Numer telefonu");
        ComboBox<String> roomField = new ComboBox<>("Pokój");
        roomField.setItems(rooms);
        DatePicker checkInField = new DatePicker("Check-in");
        DatePicker checkOutField = new DatePicker("Check-out");
        Button addReservationButton = new Button("Zapisz");
        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        addReservationButton.addClickListener(e -> {
            String guestName = guestNameField.getValue();
            String phone = phoneField.getValue();
            String room = roomField.getValue();
            LocalDate checkIn = checkInField.getValue();
            LocalDate checkOut = checkOutField.getValue();

            if (guestName.isEmpty() || phone.isEmpty() || room == null || checkIn == null || checkOut == null) {
                return; // Możesz dodać komunikat o błędzie
            }

            reservations.add(new ReservationRow(room, checkIn, checkOut, guestName, phone));
            dialog.close();
            refresh();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(addReservationButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(guestNameField, phoneField, roomField, checkInField, checkOutField, buttonsLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void initMockData() {
        rooms.add("101");
        rooms.add("102");
        rooms.add("103");
        rooms.add("104");

        YearMonth now = YearMonth.now();

        reservations.add(new ReservationRow("101",
                LocalDate.of(now.getYear(), now.getMonthValue(), 3),
                LocalDate.of(now.getYear(), now.getMonthValue(), 6),
                "Jan Kowalski",
                "123456789"
        ));

        reservations.add(new ReservationRow("102",
                LocalDate.of(now.getYear(), now.getMonthValue(), 10),
                LocalDate.of(now.getYear(), now.getMonthValue(), 15),
                "Anna Nowak",
                "987654321"
        ));

        reservations.add(new ReservationRow("103",
                LocalDate.of(now.getYear(), now.getMonthValue(), 20),
                LocalDate.of(now.getYear(), now.getMonthValue(), 22),
                "Piotr Wiśniewski",
                "555666777"
        ));
    }

    private void refresh() {
        reservationGrid.removeAllColumns();

        YearMonth selectedMonth = monthPicker.getValue();
        reservationGrid.addColumn(ReservationRow::getRoom).setHeader("Pokój");

        int days = selectedMonth.lengthOfMonth();

        for (int day = 1; day <= days; day++) {
            final int d = day;

            reservationGrid.addComponentColumn(row -> {
                LocalDate cellDate = LocalDate.of(
                        selectedMonth.getYear(),
                        selectedMonth.getMonthValue(),
                        d
                );

                boolean booked = reservations.stream().anyMatch(r ->
                        r.getRoom().equals(row.getRoom()) &&
                                r.getCheckIn() != null &&
                                r.getCheckOut() != null &&
                                !r.getCheckIn().isAfter(cellDate) &&
                                !r.getCheckOut().isBefore(cellDate)
                );

                Span cell = new Span(booked ? "X" : "");
                cell.getStyle()
                        .set("display", "block")
                        .set("text-align", "center")
                        .set("background-color", booked ? "lightblue" : "transparent");

                return cell;
            }).setHeader(String.valueOf(day));
        }

        List<ReservationRow> rows = new ArrayList<>();
        rooms.forEach(room -> rows.add(new ReservationRow(room, null, null, null, null)));

        reservationGrid.setItems(rows);
    }

    public static class ReservationRow {
        private String room;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private String guestName;
        private String phone;

        public ReservationRow(String room, LocalDate checkIn, LocalDate checkOut) {
            this(room, checkIn, checkOut, null, null);
        }

        public ReservationRow(String room, LocalDate checkIn, LocalDate checkOut, String guestName, String phone) {
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.guestName = guestName;
            this.phone = phone;
        }

        public String getRoom() { return room; }
        public LocalDate getCheckIn() { return checkIn; }
        public LocalDate getCheckOut() { return checkOut; }
        public String getGuestName() { return guestName; }
        public String getPhone() { return phone; }
    }
}
