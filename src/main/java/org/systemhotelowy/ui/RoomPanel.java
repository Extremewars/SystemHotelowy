package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.*;

public class RoomPanel extends VerticalLayout {

    private Grid<RoomRow> roomGrid;
    private List<RoomRow> rooms = new ArrayList<>();
    private Set<RoomRow> selectedRooms = new HashSet<>();

    // Panel akcji dla zaznaczonych pokoi
    private HorizontalLayout selectionActions;
    private Span selectedInfo;

    public RoomPanel() {
        setWidthFull();
        setSpacing(true);

        ComboBox<String> statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Wolny", "Zajęty", "Awaria");

        ComboBox<String> workerFilter = new ComboBox<>("Pracownik");
        workerFilter.setItems("Anna", "Jan", "Maria", "Brak przypisania");

        HorizontalLayout filters = new HorizontalLayout(statusFilter, workerFilter);
        filters.setWidthFull();
        filters.setSpacing(true);

        Button addRoomBtn = new Button("Dodaj pokój", e -> openAddRoomDialog());

        HorizontalLayout buttons = new HorizontalLayout(addRoomBtn);
        buttons.setSpacing(true);

        // ============================
        //  PANEL DLA ZAZNACZONYCH POKOI
        // ============================
        Button addTaskBtn = new Button("Dodaj zadanie");
        Button addNoteBtn = new Button("Dodaj uwagę");

        selectedInfo = new Span("Zaznaczono: 0");

        selectionActions = new HorizontalLayout(selectedInfo, addTaskBtn, addNoteBtn);
        selectionActions.setVisible(false);
        selectionActions.setSpacing(true);

        // ============================
        //         GRID
        // ============================
        roomGrid = new Grid<>(RoomRow.class, false);
        roomGrid.setWidthFull();

        // Checkbox column
        roomGrid.addComponentColumn(room -> {
            Checkbox box = new Checkbox();
            box.addValueChangeListener(e -> {
                if (e.getValue()) {
                    selectedRooms.add(room);
                } else {
                    selectedRooms.remove(room);
                }
                updateSelectionActions();
            });
            return box;
        }).setHeader("Select").setAutoWidth(true);

        // Room column
        roomGrid.addColumn(RoomRow::getRoom).setHeader("Pokój");

        // Icon-based status
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

        roomGrid.addColumn(RoomRow::getWorker).setHeader("Pracownik");
        roomGrid.addColumn(RoomRow::getTasks).setHeader("Zadania");
        roomGrid.addColumn(RoomRow::getNotes).setHeader("Uwagi");

        // Action buttons
        roomGrid.addComponentColumn(room -> {
            Button edit = new Button("Edytuj", e -> Notification.show("Edytuj " + room.getRoom()));
            Button delete = new Button("Usuń", e -> Notification.show("Usuń " + room.getRoom()));
            Button fail = new Button("Awaria", e -> Notification.show("Zgłoszono awarię w pokoju " + room.getRoom()));
            return new HorizontalLayout(edit, delete, fail);
        }).setHeader("Akcje");

        roomGrid.setItems(
                new RoomRow("101", "Wolny", "Anna", "-", ""),
                new RoomRow("102", "Zajęty", "Jan", "-", "Gość przebywa"),
                new RoomRow("103", "Wolny", "Maria", "-", ""),
                new RoomRow("104", "Awaria", "Brak", "-", "Zepsuta klimatyzacja")
        );

        add(filters, buttons, selectionActions, roomGrid);
    }

    private void updateSelectionActions() {
        int count = selectedRooms.size();
        selectedInfo.setText("Zaznaczono: " + count);
        selectionActions.setVisible(count > 0);
    }

    // ============================
    //     DIALOG "DODAJ POKÓJ"
    // ============================
    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField roomNumber = new TextField("Numer pokoju");

        ComboBox<String> worker = new ComboBox<>("Pracownik");
        worker.setItems("Anna", "Jan", "Maria", "Brak przypisania");

        TextField maxPeople = new TextField("Max osób");
        TextField price = new TextField("Cena za dobę");

        TextField location = new TextField("Lokalizacja pokoju");
        TextArea equipment = new TextArea("Wyposażenie");

        equipment.setHeight("120px");

        form.add(roomNumber, worker, maxPeople, price, location, equipment);

        Button save = new Button("Zapisz", e -> {
            roomGrid.getListDataView().addItem(new RoomRow(
                    roomNumber.getValue(),
                    "Wolny",
                    worker.getValue(),
                    "-",
                    ""
            ));
            dialog.close();
            Notification.show("Pokój dodany!");
        });

        Button cancel = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(form, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    // ============================
    //     MODEL POKOJU
    // ============================
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
}
