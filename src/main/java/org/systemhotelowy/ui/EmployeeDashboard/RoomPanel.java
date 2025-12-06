package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.ui.Report;
import org.systemhotelowy.ui.EmployeeDashboard.RoomRow;
import org.systemhotelowy.ui.Task;

import java.util.ArrayList;
import java.util.List;

public class RoomPanel extends VerticalLayout {

    private Grid<RoomRow> roomGrid;
    private List<RoomRow> rooms = new ArrayList<>();

    public RoomPanel() {
        setWidthFull();
        setSpacing(true);

        // ============================
        // FILTRY
        // ============================

        ComboBox<String> statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Wolny", "Zajęty", "Awaria");

        TextField searchField = new TextField("Szukaj pokoju");
        searchField.setPlaceholder("np. 101");
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            String value = e.getValue().trim();
            roomGrid.setItems(rooms.stream()
                    .filter(r -> r.getRoom().contains(value))
                    .toList());
        });

        HorizontalLayout topBar = new HorizontalLayout(
                statusFilter,
                searchField
        );

        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);

        // ============================
        // GRID
        // ============================

        roomGrid = new Grid<>(RoomRow.class, false);
        roomGrid.setWidthFull();

        // Pokój
        roomGrid.addColumn(RoomRow::getRoom)
                .setHeader("Pokój")
                .setWidth("100px")
                .setFlexGrow(0);

        // Status
        roomGrid.addComponentColumn(room -> {
                    Span s = new Span(room.getStatus());
                    switch (room.getStatus()) {
                        case "Wolny" -> s.getStyle().set("color", "green");
                        case "Zajęty" -> s.getStyle().set("color", "blue");
                        case "Awaria" -> s.getStyle().set("color", "red");
                    }
                    return s;
                }).setHeader("Status")
                .setWidth("110px")
                .setFlexGrow(0);
        // Max osób
        roomGrid.addColumn(RoomRow::getMaxPeople)
                .setHeader("Max osób")
                .setWidth("100px")
                .setFlexGrow(0);

        // Lokalizacja
        roomGrid.addColumn(RoomRow::getLocation)
                .setHeader("Lokalizacja")
                .setWidth("150px")
                .setFlexGrow(0);

        // Wyposażenie
        roomGrid.addColumn(RoomRow::getEquipment)
                .setHeader("Wyposażenie")
                .setWidth("200px")
                .setFlexGrow(1);



        // ZADANIA
        roomGrid.addComponentColumn(room -> {
                    VerticalLayout layout = new VerticalLayout();
                    layout.setPadding(false);
                    layout.setSpacing(false);

                    for (Task task : room.getTasks()) {
                        Span label = new Span(task.getTitle() + " (" + task.getStatus() + ")");
                        label.getStyle().set("cursor", "pointer");

                        label.addClickListener(e -> openTaskDialog(room, task));
                        layout.add(label);
                    }

                    return layout;
                }).setHeader("Zadania")
                .setWidth("200px")
                .setFlexGrow(1);

        // ZGŁOSZENIA
        roomGrid.addComponentColumn(room -> {
                    VerticalLayout layout = new VerticalLayout();
                    layout.setPadding(false);
                    layout.setSpacing(false);

                    for (Report rep : room.getReports()) {
                        Span label = new Span(rep.getTitle());
                        label.getStyle().set("cursor", "pointer");
                        label.getStyle().set("color", rep.getStatus().equals("Nowe") ? "red" : "gray");

                        label.addClickListener(e -> openReportDialog(rep));
                        layout.add(label);
                    }
                    return layout;
                }).setHeader("Zgłoszenia")
                .setWidth("200px")
                .setFlexGrow(1);

        // AKCJE
        roomGrid.addComponentColumn(room -> {
                    Button addReport = new Button("Dodaj zgłoszenie", e -> openAddReportDialog(room));
                    return new HorizontalLayout(addReport);
                }).setHeader("Akcje")
                .setWidth("180px")
                .setFlexGrow(0);

        // ============================
        // Mockowe dane
        // ============================
        RoomRow r1 = new RoomRow("101", "Wolny", "1 piętro", "TV, Klimatyzacja", "2");
        r1.addReport(new Report("Brak ręczników", "W pokoju brakuje ręczników.", "Nowe"));


        r1.addTask(new Task("Wymiana ręczników", "Wymienić zestaw ręczników dla nowych gości.", "W trakcie"));
        r1.addTask(new Task("Odświeżenie łazienki", "Umyć kabinę prysznicową i uzupełnić kosmetyki.", "Wykonane"));

        RoomRow r2 = new RoomRow("102", "Zajęty", "1 piętro", "TV", "3");
        r2.addTask(new Task("Zmiana pościeli", "Wymienić pościel po wyjeździe poprzednich gości.", "W trakcie"));

        RoomRow r3 = new RoomRow("103", "Wolny", "2 piętro", "Klimatyzacja", "2");
        r3.addTask(new Task("Sprawdzenie klimatyzacji", "Technik ma sprawdzić działanie klimatyzacji.", "W trakcie"));

        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);


        roomGrid.setItems(rooms);

        add(topBar, roomGrid);
    }

    // =====================================================
    // DIALOG: Zadanie
    // =====================================================
    private void openTaskDialog(RoomRow room, Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Span title = new Span("Tytuł: " + task.getTitle());
        Span desc = new Span("Treść: " + task.getDescription());

        // Tworzymy przycisk w zależności od statusu zadania
        Button toggleStatus = new Button();
        if ("Wykonane".equals(task.getStatus())) {
            toggleStatus.setText("Oznacz jako niewykonane");
            toggleStatus.addClickListener(e -> {
                task.setStatus("W trakcie");
                roomGrid.getDataProvider().refreshAll();
                dialog.close();
            });
        } else {
            toggleStatus.setText("Oznacz jako wykonane");
            toggleStatus.addClickListener(e -> {
                task.setStatus("Wykonane");
                roomGrid.getDataProvider().refreshAll();
                dialog.close();
            });
        }

        dialog.add(new VerticalLayout(title, desc, toggleStatus));
        dialog.open();
    }


    // =====================================================
    // DIALOG: Zgłoszenie
    // =====================================================
    private void openReportDialog(Report rep) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Span title = new Span("Tytuł: " + rep.getTitle());
        Span content = new Span("Treść: " + rep.getContent());

        dialog.add(new VerticalLayout(title, content));
        dialog.open();
    }

    // =====================================================
    // DIALOG: Dodawanie zgłoszenia
    // =====================================================
    private void openAddReportDialog(RoomRow room) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        TextField title = new TextField("Tytuł zgłoszenia");
        TextArea content = new TextArea("Treść");
        content.setHeight("150px");

        Button save = new Button("Zapisz", e -> {
            room.addReport(new Report(title.getValue(), content.getValue(), "Nowe"));
            roomGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Zgłoszenie dodane");
        });

        dialog.add(new VerticalLayout(title, content, save));
        dialog.open();
    }
}
