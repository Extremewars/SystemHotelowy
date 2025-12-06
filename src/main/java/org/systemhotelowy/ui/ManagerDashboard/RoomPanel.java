package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.ui.Report;
import org.systemhotelowy.ui.Task;

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
            Span s = new Span(room.getStatus());
            switch (room.getStatus()) {
                case "Wolny" -> s.getStyle().set("color", "green");
                case "Zajęty" -> s.getStyle().set("color", "blue");
                case "Awaria" -> s.getStyle().set("color", "red");
            }
            return s;
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
        Column<RoomRow> notesCol = roomGrid.addComponentColumn(room -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);

            for (Report rep : room.getReports()) {
                Span repLabel = new Span(rep.getTitle() + " (" + rep.getStatus() + ")");
                repLabel.getStyle().set("cursor", "pointer");

                // Kolor statusu
                if (rep.getStatus().equals("Nowe")) {
                    repLabel.getStyle().set("color", "red");
                } else {
                    repLabel.getStyle().set("color", "gray");
                }

                repLabel.addClickListener(e -> openReportDialog(room, rep));

                layout.add(repLabel);
            }

            return layout;
        }).setHeader("Zgłoszenia");

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

        // ============================
        // STYLE
        // ============================
        roomGrid.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");
        roomGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        roomGrid.addThemeVariants(
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        roomGrid.getStyle().set("margin-top", "10px");

        // =========================
        // MOCKOWE DANE
        // =========================

        RoomRow r1 = new RoomRow("101", "Wolny", "Anna", "-", "");
        r1.addReport(new Report("Brak ręczników", "W pokoju 101 brakuje ręczników.", "Nowe"));
        r1.addReport(new Report("Uszkodzona żarówka", "Przepaliła się żarówka nad łóżkiem.", "Przeczytane"));

        RoomRow r2 = new RoomRow("102", "Zajęty", "Jan", "-", "");
        RoomRow r3 = new RoomRow("103", "Wolny", "Maria", "-", "");
        RoomRow r4 = new RoomRow("104", "Awaria", "Brak", "-", "");
        r4.addReport(new Report("Klimatyzacja nie działa", "Gość zgłosił brak chłodzenia.", "Nowe"));



    // Pokój 101
        r1.addTask(new Task(
                "Wymiana ręczników",
                "Wymienić zestaw ręczników dla nowych gości.",
                "W trakcie"
        ));

        r1.addTask(new Task(
                "Odświeżenie łazienki",
                "Umyć kabinę prysznicową i uzupełnić kosmetyki.",
                "Wykonane"
        ));

    // Pokój 102
        r2.addTask(new Task(
                "Zmiana pościeli",
                "Wymienić pościel po wyjeździe poprzednich gości.",
                "W trakcie"
        ));

    // Pokój 104 (Awaria)
        r4.addTask(new Task(
                "Sprawdzenie klimatyzacji",
                "Technik ma sprawdzić przyczynę usterki klimatyzacji.",
                "W trakcie"
        ));

        r4.addTask(new Task(
                "Zgłoszenie do serwisu",
                "Przekazać zgłoszenie awarii do zewnętrznego serwisu.",
                "Wykonane"
        ));

        roomGrid.setItems(r1, r2, r3, r4);


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

    private void openReportDialog(RoomRow room, Report rep) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Span title = new Span("Tytuł: " + rep.getTitle());
        Span status = new Span("Status: " + rep.getStatus());
        Span content = new Span("Treść: " + rep.getContent());

        Button markRead = new Button("Oznacz jako przeczytane", e -> {
            rep.setStatus("Przeczytane");
            roomGrid.getDataProvider().refreshAll();
            dialog.close();
        });

        Button delete = new Button("Usuń zgłoszenie", e -> {
            room.removeReport(rep);
            roomGrid.getDataProvider().refreshAll();
            dialog.close();
        });

        dialog.add(new VerticalLayout(title, status, content, markRead, delete));
        dialog.open();
    }



}
