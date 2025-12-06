package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

public class RoomPanel extends VerticalLayout {

    private Grid<RoomRow> roomGrid;
    private List<RoomRow> rooms = new ArrayList<>();

    public RoomPanel() {
        setWidthFull();
        setSpacing(true);

        // =========================
        //       FILTRY I PRZYCISKI
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

        Button addRoomBtn = new Button("Dodaj pokój", e -> openAddRoomDialog());
        Button addTaskBtn = new Button("Dodaj zadanie", e -> Notification.show("Dodaj zadanie"));

        HorizontalLayout buttons = new HorizontalLayout(addRoomBtn, addTaskBtn);
        buttons.setSpacing(true);

        // =========================
        //       TABELA POKOI
        // =========================
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
        roomGrid.addColumn(RoomRow::getWorker).setHeader("Pracownik");
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

        add(filters, buttons, roomGrid);
    }

    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField roomNumber = new TextField("Numer pokoju");
        ComboBox<String> worker = new ComboBox<>("Pracownik");
        worker.setItems("Anna", "Jan", "Maria", "Brak przypisania");

        form.add(roomNumber, worker);

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
