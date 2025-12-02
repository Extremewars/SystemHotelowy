package org.systemhotelowy.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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

        monthPicker = new ComboBox<>("Wybierz miesiąc");
        YearMonth now = YearMonth.now();

        monthPicker.setItems(now, now.plusMonths(1), now.plusMonths(2));
        monthPicker.setValue(now);
        monthPicker.addValueChangeListener(e -> refresh());

        reservationGrid = new Grid<>(ReservationRow.class, false);
        reservationGrid.setWidthFull();

        HorizontalLayout header = new HorizontalLayout(monthPicker);
        refresh();

        add(header, reservationGrid);
    }

    private void initMockData() {
        rooms.add("101");
        rooms.add("102");
        rooms.add("103");
        rooms.add("104");

        YearMonth now = YearMonth.now();

        reservations.add(new ReservationRow("101",
                LocalDate.of(now.getYear(), now.getMonthValue(), 3),
                LocalDate.of(now.getYear(), now.getMonthValue(), 6)
        ));

        reservations.add(new ReservationRow("102",
                LocalDate.of(now.getYear(), now.getMonthValue(), 10),
                LocalDate.of(now.getYear(), now.getMonthValue(), 15)
        ));

        reservations.add(new ReservationRow("103",
                LocalDate.of(now.getYear(), now.getMonthValue(), 20),
                LocalDate.of(now.getYear(), now.getMonthValue(), 22)
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
        rooms.forEach(room -> rows.add(new ReservationRow(room, null, null)));

        reservationGrid.setItems(rows);
    }

    // Możesz wynieść klasę row także do osobnego pliku
    public static class ReservationRow {
        private String room;
        private LocalDate checkIn;
        private LocalDate checkOut;

        public ReservationRow(String room, LocalDate checkIn, LocalDate checkOut) {
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public String getRoom() { return room; }
        public LocalDate getCheckIn() { return checkIn; }
        public LocalDate getCheckOut() { return checkOut; }
    }
}
