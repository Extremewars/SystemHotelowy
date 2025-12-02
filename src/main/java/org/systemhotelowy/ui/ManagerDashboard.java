package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Route("manager")
@PageTitle("Panel Kierownika")
public class ManagerDashboard extends VerticalLayout {

    private Grid<RoomRow> roomGrid;
    private Grid<ReservationRow> reservationGrid;
    private ComboBox<YearMonth> monthPicker;
    private List<ReservationRow> reservations = new ArrayList<>();

    public ManagerDashboard() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK
        // =========================
        H1 title = new H1("Panel Kierownika");
        Span loggedUser = new Span("Zalogowano jako: kierownik@hotel.pl");
        Button logoutBtn = new Button("Wyloguj", e -> Notification.show("Wylogowano!"));

        HorizontalLayout rightSide = new HorizontalLayout(loggedUser, logoutBtn);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout topBar = new HorizontalLayout(title, rightSide);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        add(topBar);

        // =========================
        //       PANEL KPI
        // =========================
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        kpiLayout.add(createKpiBox("Wolne", 10, VaadinIcon.CHECK_CIRCLE));
        kpiLayout.add(createKpiBox("Zajęte", 5, VaadinIcon.USER));
        kpiLayout.add(createKpiBox("Awaria", 1, VaadinIcon.WARNING));


        // =========================
        //       TABELA POKOI
        // =========================
        Button addRoomBtn = new Button("Dodaj pokój", e -> openAddRoomDialog());
        Button addTaskBtn = new Button("Dodaj zadanie", e -> openAddTaskDialog());
        HorizontalLayout roomButtons = new HorizontalLayout(addRoomBtn, addTaskBtn);
        roomButtons.setSpacing(true);

        roomGrid = new Grid<>(RoomRow.class, false);
        roomGrid.setWidthFull();

        roomGrid.addComponentColumn(room -> {
            Icon icon;
            switch (room.getStatus()) {
                case "Wolny":
                    icon = VaadinIcon.CHECK_CIRCLE.create();
                    icon.setColor("green");
                    break;
                case "Zajęty":
                    icon = VaadinIcon.USER.create();
                    icon.setColor("blue");
                    break;
                case "Awaria":
                    icon = VaadinIcon.WARNING.create();
                    icon.setColor("red");
                    break;
                default:
                    icon = VaadinIcon.QUESTION.create();
                    icon.setColor("gray");
            }
            return icon;
        }).setHeader("Status");

        roomGrid.addColumn(RoomRow::getRoom).setHeader("Pokój");
        roomGrid.addColumn(RoomRow::getWorker).setHeader("Osoba");
        roomGrid.addColumn(RoomRow::getTasks).setHeader("Zadania");
        roomGrid.addColumn(RoomRow::getNotes).setHeader("Uwagi");

        roomGrid.addComponentColumn(room -> {
            Button edit = new Button("Edytuj", e -> Notification.show("Edytuj " + room.getRoom()));
            Button delete = new Button("Usuń", e -> Notification.show("Usuń " + room.getRoom()));
            return new HorizontalLayout(edit, delete);
        }).setHeader("Akcje");

        roomGrid.setItems(
                new RoomRow("101", "Wolny", "Anna", "-", ""),
                new RoomRow("102", "Zajęty", "Jan", "-", "Gość przebywa"),
                new RoomRow("103", "Wolny", "Maria", "-", ""),
                new RoomRow("104", "Awaria", "Brak", "-", "Zepsuta klimatyzacja")
        );

        VerticalLayout roomSection = new VerticalLayout(roomButtons, roomGrid);
        roomSection.setWidthFull();

        // =========================
        //       TABELA REZERWACJI
        // =========================
        Button addReservationBtn = new Button("Dodaj rezerwację", e -> openAddReservationDialog());
        monthPicker = new ComboBox<>("Wybierz miesiąc");
        monthPicker.setItems(
                YearMonth.now(),
                YearMonth.now().plusMonths(1),
                YearMonth.now().plusMonths(2)
        );
        monthPicker.setValue(YearMonth.now());
        monthPicker.addValueChangeListener(e -> refreshReservationGrid());

        HorizontalLayout reservationHeader = new HorizontalLayout(addReservationBtn, monthPicker);
        reservationHeader.setSpacing(true);

        reservationGrid = new Grid<>(ReservationRow.class, false);
        reservationGrid.setWidthFull();

        refreshReservationGrid(); // teraz roomGrid jest już zainicjalizowany

        add(kpiLayout, reservationHeader, reservationGrid, roomSection);

        // =========================
        //       FILTRY POKOI
        // =========================
        ComboBox<String> statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Wolny", "Zajęty", "Awaria");

        ComboBox<String> workerFilter = new ComboBox<>("Pracownik");
        workerFilter.setItems("Anna", "Jan", "Maria", "Brak przypisania");

        ComboBox<Integer> floorFilter = new ComboBox<>("Piętro");
        floorFilter.setItems(0, 1, 2, 3, 4, 5);

        HorizontalLayout filters = new HorizontalLayout(statusFilter, workerFilter, floorFilter);
        filters.setWidthFull();
        filters.setSpacing(true);

        add(filters);

        expand(roomSection);
    }

    // =========================
    //    PANEL KPI
    // =========================
    private Div createKpiBox(String title, int value, VaadinIcon iconType) {
        Div box = new Div();
        box.getStyle().set("border", "1px solid #ccc");
        box.getStyle().set("padding", "10px");
        box.getStyle().set("border-radius", "5px");
        box.getStyle().set("text-align", "center");
        box.getStyle().set("flex", "1");

        Icon icon = iconType.create();
        icon.setSize("32px");

        Span t = new Span(title);
        t.getStyle().set("display", "block");
        Span v = new Span(String.valueOf(value));
        v.getStyle().set("font-weight", "bold");

        box.add(icon, t, v);
        return box;
    }

    // =========================
    //    FORMULARZ DODAWANIA POKOJU
    // =========================
    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField roomNumber = new TextField("Numer pokoju");
        ComboBox<String> status = new ComboBox<>("Status");
        status.setItems("Wolny", "Zajęty", "Awaria");
        ComboBox<String> worker = new ComboBox<>("Pracownik");
        worker.setItems("Anna", "Jan", "Maria", "Brak przypisania");

        form.add(roomNumber, status, worker);

        Button save = new Button("Zapisz", e -> {
            roomGrid.getListDataView().addItem(new RoomRow(
                    roomNumber.getValue(),
                    status.getValue(),
                    worker.getValue(),
                    "-",
                    ""
            ));
            dialog.close();
            Notification.show("Pokój dodany!");
            refreshReservationGrid(); // odśwież rezerwacje, jeśli dodano nowy pokój
        });

        Button cancel = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(form, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    // =========================
    //    FORMULARZ DODAWANIA ZADANIA
    // =========================
    private void openAddTaskDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField taskName = new TextField("Nazwa zadania");
        TextArea taskDesc = new TextArea("Opis zadania");

        form.add(taskName, taskDesc);

        Button save = new Button("Zapisz", e -> {
            Notification.show("Zadanie dodane: " + taskName.getValue());
            dialog.close();
        });

        Button cancel = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(form, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    // =========================
    //    FORMULARZ DODAWANIA REZERWACJI
    // =========================
    private void openAddReservationDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField roomNumber = new TextField("Numer pokoju");
        DatePicker checkIn = new DatePicker("Check-in");
        DatePicker checkOut = new DatePicker("Check-out");

        form.add(roomNumber, checkIn, checkOut);

        Button save = new Button("Zapisz", e -> {
            reservations.add(new ReservationRow(roomNumber.getValue(), checkIn.getValue(), checkOut.getValue()));
            refreshReservationGrid();
            dialog.close();
            Notification.show("Rezerwacja dodana!");
        });

        Button cancel = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(form, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void refreshReservationGrid() {
        if (roomGrid == null || monthPicker == null) return;

        YearMonth selectedMonth = monthPicker.getValue();
        reservationGrid.removeAllColumns();

        // kolumna "Pokój" po lewej
        reservationGrid.addColumn(ReservationRow::getRoom).setHeader("Pokój");

        // kolumny dni miesiąca
        int daysInMonth = selectedMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            reservationGrid.addComponentColumn(row -> {
                LocalDate cellDate = LocalDate.of(selectedMonth.getYear(), selectedMonth.getMonthValue(), currentDay);
                boolean booked = reservations.stream()
                        .anyMatch(r -> r.getRoom().equals(row.getRoom())
                                && r.getCheckIn() != null
                                && r.getCheckOut() != null
                                && !r.getCheckIn().isAfter(cellDate)
                                && !r.getCheckOut().isBefore(cellDate));
                Span cell = new Span(booked ? "X" : "");
                cell.getStyle().set("display", "block")
                        .set("text-align", "center")
                        .set("background-color", booked ? "lightblue" : "transparent");
                return cell;
            }).setHeader(day + " " + selectedMonth.getMonthValue());
        }

        // wiersze: jeden na każdy pokój
        List<ReservationRow> monthRows = new ArrayList<>();
        for (RoomRow room : roomGrid.getListDataView().getItems().toList()) {
            monthRows.add(new ReservationRow(room.getRoom(), null, null));
        }
        reservationGrid.setItems(monthRows);
    }


    // =========================
    //      PROSTA KLASA DO GRIDU POKOI
    // =========================
    public static class RoomRow {
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

    // =========================
    //      PROSTA KLASA DO GRIDU REZERWACJI
    // =========================
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
