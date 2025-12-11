package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.model.Reservation;
import org.systemhotelowy.model.ReservationStatus;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.service.ReservationService;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.ui.components.ReservationFormDialog;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kalendarz rezerwacji z prawdziwymi danymi z bazy.
 */
public class ReservationCalendar extends VerticalLayout {

    private final ReservationService reservationService;
    private final RoomService roomService;

    private Grid<RoomRow> reservationGrid;

    private List<Room> rooms = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    private LocalDate windowStart = LocalDate.now().minusDays(1);
    private final int WINDOW_SIZE = 14;

    private Span rangeLabel;

    public ReservationCalendar(ReservationService reservationService, RoomService roomService) {
        this.reservationService = reservationService;
        this.roomService = roomService;

        setSpacing(true);
        setPadding(true);

        // Załaduj dane z bazy
        loadData();

        reservationGrid = new Grid<>(RoomRow.class, false);
        reservationGrid.setWidthFull();
        reservationGrid.getStyle()
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        // ======= PAGINATION =======
        Button prevBtn = new Button("<", e -> {
            windowStart = windowStart.minusDays(WINDOW_SIZE);
            refresh();
        });

        Button nextBtn = new Button(">", e -> {
            windowStart = windowStart.plusDays(WINDOW_SIZE);
            refresh();
        });

        rangeLabel = new Span();
        rangeLabel.setId("range-label");
        rangeLabel.getStyle().set("font-weight", "600").set("font-size", "14px");

        HorizontalLayout pager = new HorizontalLayout(prevBtn, rangeLabel, nextBtn);
        pager.setAlignItems(Alignment.CENTER);

        Button openFormButton = new Button("Dodaj rezerwację");
        openFormButton.addClickListener(e -> openReservationDialog(null, null));

        HorizontalLayout header = new HorizontalLayout(pager, openFormButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        refresh();
        add(header, reservationGrid);
        // ============================
        // STYLE
        // ============================
        reservationGrid.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");
        reservationGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        reservationGrid.addThemeVariants(
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        reservationGrid.getStyle().set("margin-top", "10px");
    }

    // ------------------------------------------------------------------------
    // CLICK HANDLING
    // ------------------------------------------------------------------------

    /**
     * Klasa pomocnicza reprezentująca wiersz w gridzie (pokój).
     */
    private static class RoomRow {
        private final Room room;

        public RoomRow(Room room) {
            this.room = room;
        }

        public Room getRoom() {
            return room;
        }

        public String getRoomNumber() {
            return room.getNumber();
        }
    }

    private void loadData() {
        rooms = roomService.findAll();
        LocalDate start = windowStart;
        LocalDate end = windowStart.plusDays(WINDOW_SIZE);
        reservations = reservationService.findReservationsInPeriod(start, end);
    }

    private void handleCellClick(Room room, LocalDate day) {
        Optional<Reservation> found = reservations.stream()
                .filter(r ->
                        r.getRoom().getId().equals(room.getId()) &&
                                !r.getCheckInDate().isAfter(day) &&
                                !r.getCheckOutDate().isBefore(day)
                )
                .findFirst();

        if (found.isPresent()) {
            new ReservationFormDialog(found.get(), rooms, reservationService, () -> {
                loadData();
                refresh();
            }).open();
        } else {
            new ReservationFormDialog(room, day, rooms, reservationService, () -> {
                loadData();
                refresh();
            }).open();
        }
    }

    // ------------------------------------------------------------------------
    // ADD RESERVATION DIALOG
    // ------------------------------------------------------------------------

    private void openReservationDialog(Room preselectedRoom, LocalDate preselectedDay) {
        new ReservationFormDialog(preselectedRoom, preselectedDay, rooms, reservationService, () -> {
            loadData();
            refresh();
        }).open();
    }

    private String formatStatus(ReservationStatus status) {
        switch (status) {
            case PENDING:
                return "Oczekująca";
            case CONFIRMED:
                return "Potwierdzona";
            case CHECKED_IN:
                return "Zameldowany";
            case CHECKED_OUT:
                return "Wymeldowany";
            case CANCELLED:
                return "Anulowana";
            default:
                return status.name();
        }
    }


    // ------------------------------------------------------------------------
    // DATE RANGE FORMATTER
    // ------------------------------------------------------------------------

    private String formatRange(LocalDate start, LocalDate end) {
        Locale pl = Locale.forLanguageTag("pl");
        return start.getDayOfMonth() + " " +
                start.getMonth().getDisplayName(TextStyle.FULL, pl) +
                " " + start.getYear() +
                " – " +
                end.getDayOfMonth() + " " +
                end.getMonth().getDisplayName(TextStyle.FULL, pl) +
                " " + end.getYear();
    }

    // ------------------------------------------------------------------------
    // GRID REFRESH
    // ------------------------------------------------------------------------

    private void refresh() {
        // Przeładuj dane
        loadData();

        reservationGrid.removeAllColumns();

        // ---- Left columns (narrow, fixed) ----
        reservationGrid.addColumn(RoomRow::getRoomNumber)
                .setHeader("Pokój")
                .setWidth("70px")
                .setFlexGrow(0);


        // ---- Date columns ----
        for (int i = 0; i < WINDOW_SIZE; i++) {
            LocalDate currentDay = windowStart.plusDays(i);

            String dow = currentDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pl"));
            String headerHtml = "<div style='text-align:center;font-size:12px'><b>" +
                    dow + "</b><br/>" + currentDay.getDayOfMonth() + "</div>";

            Span header = new Span();
            header.getElement().setProperty("innerHTML", headerHtml);

            reservationGrid.addComponentColumn(row -> createCell(row, currentDay))
                    .setHeader(header)
                    .setFlexGrow(1);
        }

        List<RoomRow> rows = rooms.stream()
                .map(RoomRow::new)
                .collect(Collectors.toList());

        reservationGrid.setItems(rows);

        // ---- Update range label ----
        LocalDate start = windowStart;
        LocalDate end = windowStart.plusDays(WINDOW_SIZE - 1);
        rangeLabel.setText(formatRange(start, end));
    }

    // ------------------------------------------------------------------------
    // CELL RENDERING
    // ------------------------------------------------------------------------

    private Div createCell(RoomRow row, LocalDate day) {
        Div cell = new Div();
        cell.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("padding", "6px")
                .set("border-radius", "4px")
                .set("transition", "background-color 0.15s")
                .set("cursor", "pointer")
                .set("height", "26px");

        Room room = row.getRoom();

        Optional<Reservation> res = reservations.stream()
                .filter(r ->
                        r.getRoom().getId().equals(room.getId()) &&
                                !r.getCheckInDate().isAfter(day) &&
                                !r.getCheckOutDate().isBefore(day) &&
                                r.getStatus() != ReservationStatus.CANCELLED
                )
                .findFirst();

        if (res.isPresent()) {
            Reservation rez = res.get();
            String color = getColorForGuest(rez.getGuestName());

            cell.getStyle().set("background-color", color);

            if (day.equals(rez.getCheckInDate()))
                cell.getStyle().set("border-top-left-radius", "12px")
                        .set("border-bottom-left-radius", "12px");

            if (day.equals(rez.getCheckOutDate()))
                cell.getStyle().set("border-top-right-radius", "12px")
                        .set("border-bottom-right-radius", "12px");

            cell.getElement().setProperty("title",
                    rez.getGuestName() + " (" + rez.getGuestPhone() + ") - " + formatStatus(rez.getStatus()));

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

    private String getColorForGuest(String guestName) {
        int hash = Math.abs(guestName.hashCode());
        int r = (hash % 128) + 100;
        int g = ((hash / 128) % 128) + 100;
        int b = ((hash / (128 * 128)) % 128) + 100;
        return "rgb(" + r + "," + g + "," + b + ")";
    }

}
