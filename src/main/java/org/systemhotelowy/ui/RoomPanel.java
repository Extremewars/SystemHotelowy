package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
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

    private HorizontalLayout selectionActions;
    private Span selectedInfo;

    public RoomPanel() {
        setWidthFull();
        setSpacing(true);

        // ============================
        // FILTRY
        // ============================
        ComboBox<String> statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Wolny", "Zajęty", "Awaria");

        ComboBox<String> workerFilter = new ComboBox<>("Pracownik");
        workerFilter.setItems("Anna", "Jan", "Maria", "Brak przypisania");
        TextField searchField = new TextField("Szukaj pokoju");
        searchField.setPlaceholder("np. 101");
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            String value = e.getValue().trim();
            roomGrid.setItems(rooms.stream()
                    .filter(r -> r.getRoom().contains(value))
                    .toList());
        });


        // Przycisk
        Button addRoomBtn = new Button("Dodaj pokój", e -> openAddRoomDialog());

        // Jedna linia: filtry po lewej, przycisk po prawej
        Span spacer = new Span();
        spacer.getStyle().set("flex-grow", "1");

        HorizontalLayout topBar = new HorizontalLayout(
                statusFilter,
                workerFilter,
                searchField,
                spacer,
                addRoomBtn
        );

        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);

        // ============================
        // PANEL ZAZNACZONYCH
        // ============================
        Button addTaskBtn = new Button("Dodaj zadanie", e -> openAddTaskDialog());


        selectedInfo = new Span("Zaznaczono: 0");

        selectionActions = new HorizontalLayout(selectedInfo, addTaskBtn);
        selectionActions.setVisible(false);
        selectionActions.setSpacing(true);

        // ============================
        // GRID
        // ============================
        roomGrid = new Grid<>(RoomRow.class, false);
        roomGrid.setWidthFull();

        // Kolumna checkbox
        Column<RoomRow> selectCol = roomGrid.addComponentColumn(room -> {
            Checkbox box = new Checkbox();
            box.addValueChangeListener(e -> {
                if (e.getValue()) selectedRooms.add(room);
                else selectedRooms.remove(room);
                updateSelectionActions();
            });
            return box;
        }).setHeader("Select");
        selectCol.setWidth("80px");
        selectCol.setFlexGrow(0);

        // Pokój
        Column<RoomRow> roomCol = roomGrid.addColumn(RoomRow::getRoom)
                .setHeader("Pokój");
        roomCol.setWidth("100px");
        roomCol.setFlexGrow(0);

        // Status
        Column<RoomRow> statusCol = roomGrid.addComponentColumn(room -> {
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
        statusCol.setWidth("110px");
        statusCol.setFlexGrow(0);

        // Pracownik
        Column<RoomRow> workerCol = roomGrid.addColumn(RoomRow::getWorker)
                .setHeader("Pracownik");
        workerCol.setWidth("130px");
        workerCol.setFlexGrow(0);

        Column<RoomRow> tasksCol = roomGrid.addComponentColumn(room -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);

            for (Task task : room.getTasks()) {
                Span taskLabel = new Span(task.getTitle() + " (" + task.getStatus() + ")");
                taskLabel.getStyle().set("cursor", "pointer");

                taskLabel.addClickListener(e -> openTaskInfoDialog(room, task));

                layout.add(taskLabel);
            }

            return layout;
        }).setHeader("Zadania");
        tasksCol.setWidth("250px");
        tasksCol.setFlexGrow(1);


        // Zgłoszenia – szerokie
        Column<RoomRow> notesCol = roomGrid.addColumn(RoomRow::getNotes)
                .setHeader("Zgłoszenia");
        notesCol.setWidth("250px");
        notesCol.setFlexGrow(1);

        // Akcje – szerokie
        Column<RoomRow> actionsCol = roomGrid.addComponentColumn(room -> {
            Button edit = new Button("Edytuj");
            Button delete = new Button("Usuń");
            Button fail = new Button("Awaria");
            return new HorizontalLayout(edit, delete, fail);
        }).setHeader("Akcje");
        actionsCol.setWidth("260px");
        actionsCol.setFlexGrow(1);

        roomGrid.setItems(
                new RoomRow("101", "Wolny", "Anna", "-", ""),
                new RoomRow("102", "Zajęty", "Jan", "-", "Brak ręczników"),
                new RoomRow("103", "Wolny", "Maria", "-", ""),
                new RoomRow("104", "Awaria", "Brak", "-", "Zepsuta klimatyzacja")
        );

        add(topBar, selectionActions, roomGrid);
    }

    private void updateSelectionActions() {
        int count = selectedRooms.size();
        selectedInfo.setText("Zaznaczono: " + count);
        selectionActions.setVisible(count > 0);
    }

    private void openAddTaskDialog() {
        if (selectedRooms.isEmpty()) {
            Notification.show("Najpierw zaznacz pokój.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        TextField title = new TextField("Tytuł zadania");
        TextArea description = new TextArea("Opis zadania");
        description.setHeight("150px");



        Button save = new Button("Zapisz", e -> {
            Task task = new Task(
                    title.getValue(),
                    description.getValue(),
                    "W trakcie"

            );

            selectedRooms.forEach(r -> r.addTask(task));
            roomGrid.getDataProvider().refreshAll();
            dialog.close();
        });

        dialog.add(new VerticalLayout(title, description, save));
        dialog.open();
    }

    private void openTaskInfoDialog(RoomRow room, Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Span title = new Span("Tytuł: " + task.getTitle());
        Span status = new Span("Status: " + task.getStatus());
        Span desc = new Span("Opis: " + task.getDescription());

        Button delete = new Button("Usuń zadanie", e -> {
            room.removeTask(task);
            roomGrid.getDataProvider().refreshAll();
            dialog.close();
        });

        dialog.add(new VerticalLayout(title, status, desc, delete));
        dialog.open();
    }



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


}
