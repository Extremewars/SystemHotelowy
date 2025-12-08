package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.systemhotelowy.model.*;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel zarządzania pokojami z prawdziwymi danymi z bazy.
 */
public class RoomPanel extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(RoomPanel.class);

    private final RoomService roomService;
    private final TaskService taskService;
    private final UserService userService;

    private Grid<Room> roomGrid;
    private List<Room> rooms = new ArrayList<>();
    private Set<Room> selectedRooms = new HashSet<>();
    private Map<Integer, List<org.systemhotelowy.model.Task>> tasksCache = new HashMap<>();
    
    // =====================================================
// METODA: Tworzy ładny boks / kafelek
// =====================================================
    private Span createBoxLabel(String text) {
        Span box = new Span(text);

        box.getStyle()
                .set("display", "inline-block")
                .set("padding", "6px 10px")
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px")
                .set("background-color", "#f4f4f4")
                .set("cursor", "pointer")
                .set("transition", "0.2s")
                .set("margin-bottom", "4px");

        // efekt hover
        box.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");

        return box;
    }


    public RoomPanel(RoomService roomService, TaskService taskService, UserService userService) {
        this.roomService = roomService;
        this.taskService = taskService;
        this.userService = userService;
        
        setWidthFull();
        setSpacing(true);

        // ============================
        // FILTRY
        // ============================
        ComboBox<RoomStatus> statusFilter = new ComboBox<>("Status");
        statusFilter.setItems(RoomStatus.values());
        statusFilter.setItemLabelGenerator(status -> {
            switch (status) {
                case READY: return "Gotowy";
                case DIRTY: return "Brudny";
                case CLEANING: return "Sprzątanie";
                case IN_MAINTENANCE: return "Konserwacja";
                case OUT_OF_ORDER: return "Awaria";
                default: return status.name();
            }
        });
        statusFilter.addValueChangeListener(e -> filterRooms(statusFilter.getValue(), null));

        TextField searchField = new TextField("Szukaj pokoju");
        searchField.setPlaceholder("np. 101");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterRooms(statusFilter.getValue(), e.getValue()));

        Button refreshBtn = new Button("Odśwież", e -> {
            loadRoomsFromDatabase();
            Notification.show("Dane odświeżone", 2000, Notification.Position.BOTTOM_START);
        });

        // Przycisk
        Button addRoomBtn = new Button("Dodaj pokój", e -> openAddRoomDialog());
        
        // Nowy przycisk do dodawania zadań dla zaznaczonych pokoi
        final Button addTasksBtn = new Button("Dodaj zadania dla zaznaczonych", e -> openAddTaskForSelectedRoomsDialog());
        addTasksBtn.setEnabled(false);

        // Jedna linia: filtry po lewej, przyciski po prawej
        Span spacer = new Span();
        spacer.getStyle().set("flex-grow", "1");

        HorizontalLayout topBar = new HorizontalLayout(
                statusFilter,
                searchField,
                spacer,
                addTasksBtn,
                refreshBtn,
                addRoomBtn
        );

        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);


        // ============================
        // GRID
        // ============================
        roomGrid = new Grid<>(Room.class, false);
        roomGrid.setWidthFull();

        // Kolumna Checkbox
        roomGrid.addComponentColumn(room -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(selectedRooms.contains(room));
            checkbox.addValueChangeListener(e -> {
                if (e.getValue()) {
                    selectedRooms.add(room);
                } else {
                    selectedRooms.remove(room);
                }
                addTasksBtn.setEnabled(!selectedRooms.isEmpty());
            });
            return checkbox;
        }).setHeader("Wybierz")
                .setWidth("80px")
                .setFlexGrow(0);

        // Kolumna Numer pokoju
        roomGrid.addColumn(Room::getNumber)
                .setHeader("Numer")
                .setWidth("100px")
                .setFlexGrow(0)
                .setSortable(true);

        // Kolumna Piętro
        roomGrid.addColumn(room -> room.getFloor() != null ? room.getFloor().toString() : "-")
                .setHeader("Piętro")
                .setWidth("80px")
                .setFlexGrow(0);

        // Kolumna Typ
        roomGrid.addColumn(room -> formatRoomType(room.getType()))
                .setHeader("Typ")
                .setWidth("120px")
                .setFlexGrow(0);

        // Kolumna Status z kolorami
        roomGrid.addComponentColumn(room -> {
            Span s = new Span(formatRoomStatus(room.getRoomStatus()));
            s.getStyle().set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-weight", "500");
            
            switch (room.getRoomStatus()) {
                case READY -> s.getStyle().set("background-color", "#d4edda").set("color", "#155724");
                case DIRTY -> s.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
                case CLEANING -> s.getStyle().set("background-color", "#cce5ff").set("color", "#004085");
                case OUT_OF_ORDER, IN_MAINTENANCE -> s.getStyle().set("background-color", "#f8d7da").set("color", "#721c24");
            }
            return s;
        }).setHeader("Status")
                .setWidth("140px")
                .setFlexGrow(0);

        // Kolumna Zadania - używa cache zamiast odpytywać bazę dla każdego renderowania
        roomGrid.addComponentColumn(room -> {
            List<org.systemhotelowy.model.Task> tasks = tasksCache.getOrDefault(room.getId(), new ArrayList<>());
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);

            if (tasks.isEmpty()) {
                layout.add(new Span("-"));
            } else {
                tasks.forEach(task -> {
                    Span taskLabel = createBoxLabel(
                        task.getDescription().length() > 30 
                            ? task.getDescription().substring(0, 30) + "..." 
                            : task.getDescription()
                    );
                    taskLabel.addClickListener(e -> openTaskDetailsDialog(task));
                    layout.add(taskLabel);
                });
            }
            return layout;
        }).setHeader("Zadania")
                .setWidth("200px")
                .setFlexGrow(1);

        // Kolumna Akcje
        roomGrid.addComponentColumn(room -> {
            Button editBtn = new Button("Edytuj", e -> openEditRoomDialog(room));
            editBtn.setTooltipText("Edytuj pokój");
            
            Button statusBtn = new Button("Status", e -> openChangeStatusDialog(room));
            statusBtn.setTooltipText("Zmień status");
            
            Button deleteBtn = new Button("Usuń", e -> deleteRoom(room));
            deleteBtn.setTooltipText("Usuń pokój");
            deleteBtn.getStyle().set("color", "red");
            
            return new HorizontalLayout(editBtn, statusBtn, deleteBtn);
        }).setHeader("Akcje")
                .setWidth("280px")
                .setFlexGrow(1);


// -----------------------------------------------------
// STYLE
// -----------------------------------------------------
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

        add(topBar, roomGrid);
        
        // Załaduj dane z bazy (MUSI BYĆ PO inicjalizacji roomGrid!)
        loadRoomsFromDatabase();
    }

    // =====================================================
    // METODY POMOCNICZE
    // =====================================================
    
    /**
     * Publiczna metoda do odświeżania danych z bazy - używana przez automatyczne odświeżanie.
     */
    public void refreshData() {
        loadRoomsFromDatabase();
    }
    
    private void loadRoomsFromDatabase() {
        rooms = roomService.findAll();
        selectedRooms.clear(); // Wyczyść zaznaczenia przy odświeżaniu
        
        // Załaduj taski dla wszystkich pokoi i zapisz w cache
        tasksCache.clear();
        rooms.forEach(room -> {
            tasksCache.put(room.getId(), taskService.findByRoomId(room.getId()));
        });
        
        // Ustaw nowe items (to resetuje DataProvider)
        roomGrid.setItems(rooms);
        
        // Dodatkowo wymuś odświeżenie wszystkich wierszy
        roomGrid.getDataProvider().refreshAll();
    }
    
    /**
     * Odświeża cache tasków dla konkretnych pokoi i przebudowuje ich wiersze w gridzie
     */
    private void refreshTasksForRooms(Set<Integer> roomIds) {
        // Zaktualizuj cache dla tych pokoi
        roomIds.forEach(roomId -> {
            tasksCache.put(roomId, taskService.findByRoomId(roomId));
        });
        
        // Wymuś przeładowanie całego grida (ComponentRenderer musi zostać wywołany ponownie)
        List<Room> currentItems = new ArrayList<>(rooms);
        roomGrid.setItems(new ArrayList<>()); // Wyczyść
        roomGrid.setItems(currentItems); // Załaduj ponownie
        roomGrid.getDataProvider().refreshAll();
    }
    


    private void filterRooms(RoomStatus status, String searchText) {
        List<Room> filtered = rooms;
        
        if (status != null) {
            filtered = filtered.stream()
                    .filter(room -> room.getRoomStatus() == status)
                    .collect(Collectors.toList());
        }
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(room -> room.getNumber().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }
        roomGrid.setItems(filtered);
    }

    private String formatRoomType(RoomType type) {
        switch (type) {
            case SINGLE: return "Pojedynczy";
            case DOUBLE: return "Podwójny";
            case SUITE: return "Apartament";
            case OTHER: return "Inny";
            default: return type.name();
        }
    }

    private String formatRoomStatus(RoomStatus status) {
        switch (status) {
            case READY: return "Gotowy";
            case DIRTY: return "Brudny";
            case CLEANING: return "Sprzątanie";
            case IN_MAINTENANCE: return "Konserwacja";
            case OUT_OF_ORDER: return "Awaria";
            default: return status.name();
        }
    }

    private void openTaskDetailsDialog(org.systemhotelowy.model.Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Szczegóły zadania");

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(new Span(task.getDescription()), "Opis");
        formLayout.addFormItem(new Span(task.getStatus().toString()), "Status");
        if (task.getRemarks() != null) {
            formLayout.addFormItem(new Span(task.getRemarks()), "Uwagi");
        }
        if (task.getAssignedTo() != null) {
            formLayout.addFormItem(
                new Span(task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName()), 
                "Przypisany do"
            );
        }
        if (task.getRequestedBy() != null) {
            formLayout.addFormItem(
                new Span(task.getRequestedBy().getFirstName() + " " + task.getRequestedBy().getLastName()), 
                "Zlecone przez"
            );
        }

        Button closeBtn = new Button("Zamknij", e -> dialog.close());
        Button deleteBtn = new Button("Usuń zadanie", e -> {
            Integer roomId = task.getRoom().getId();
            taskService.deleteById(task.getId());
            
            // Odśwież tylko ten pokój, którego zadanie zostało usunięte
            refreshTasksForRooms(Collections.singleton(roomId));
            
            dialog.close();
            showSuccess("Zadanie usunięte");
        });
        deleteBtn.getStyle().set("color", "red");

        dialog.add(formLayout);
        dialog.getFooter().add(deleteBtn, closeBtn);
        dialog.open();
    }



    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Dodaj nowy pokój");

        FormLayout form = new FormLayout();
        
        TextField roomNumber = new TextField("Numer pokoju");
        roomNumber.setRequired(true);
        
        IntegerField floorField = new IntegerField("Piętro");
        floorField.setValue(1);
        
        ComboBox<RoomType> typeCombo = new ComboBox<>("Typ pokoju");
        typeCombo.setItems(RoomType.values());
        typeCombo.setItemLabelGenerator(this::formatRoomType);
        typeCombo.setValue(RoomType.SINGLE);
        typeCombo.setRequired(true);
        
        ComboBox<RoomStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(RoomStatus.values());
        statusCombo.setItemLabelGenerator(this::formatRoomStatus);
        statusCombo.setValue(RoomStatus.DIRTY);
        statusCombo.setRequired(true);
        
        IntegerField capacityField = new IntegerField("Pojemność (liczba osób)");
        capacityField.setValue(2);
        capacityField.setMin(1);
        capacityField.setMax(10);
        capacityField.setRequired(true);

        form.add(roomNumber, floorField, typeCombo, statusCombo, capacityField);

        Button saveBtn = new Button("Zapisz", e -> {
            if (roomNumber.isEmpty()) {
                showError("Numer pokoju jest wymagany");
                return;
            }
            
            try {
                Room room = new Room();
                room.setNumber(roomNumber.getValue());
                room.setFloor(floorField.getValue());
                room.setType(typeCombo.getValue());
                room.setRoomStatus(statusCombo.getValue());
                room.setCapacity(capacityField.getValue());
                
                roomService.create(room);
                loadRoomsFromDatabase();
                dialog.close();
                showSuccess("Pokój dodany pomyślnie");
            } catch (Exception ex) {
                showError("Błąd przy dodawaniu pokoju: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openEditRoomDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Edytuj pokój " + room.getNumber());

        FormLayout form = new FormLayout();
        
        TextField roomNumber = new TextField("Numer pokoju");
        roomNumber.setValue(room.getNumber());
        
        IntegerField floorField = new IntegerField("Piętro");
        floorField.setValue(room.getFloor() != null ? room.getFloor() : 1);
        
        ComboBox<RoomType> typeCombo = new ComboBox<>("Typ pokoju");
        typeCombo.setItems(RoomType.values());
        typeCombo.setItemLabelGenerator(this::formatRoomType);
        typeCombo.setValue(room.getType());
        
        ComboBox<RoomStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(RoomStatus.values());
        statusCombo.setItemLabelGenerator(this::formatRoomStatus);
        statusCombo.setValue(room.getRoomStatus());
        
        IntegerField capacityField = new IntegerField("Pojemność (liczba osób)");
        capacityField.setValue(room.getCapacity() != null ? room.getCapacity() : 2);
        capacityField.setMin(1);
        capacityField.setMax(10);

        form.add(roomNumber, floorField, typeCombo, statusCombo, capacityField);

        Button saveBtn = new Button("Zapisz", e -> {
            try {
                room.setNumber(roomNumber.getValue());
                room.setFloor(floorField.getValue());
                room.setType(typeCombo.getValue());
                room.setRoomStatus(statusCombo.getValue());
                room.setCapacity(capacityField.getValue());
                
                roomService.update(room);
                loadRoomsFromDatabase();
                dialog.close();
                showSuccess("Pokój zaktualizowany");
            } catch (Exception ex) {
                showError("Błąd: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openChangeStatusDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setHeaderTitle("Zmień status pokoju " + room.getNumber());

        ComboBox<RoomStatus> statusCombo = new ComboBox<>("Nowy status");
        statusCombo.setItems(RoomStatus.values());
        statusCombo.setItemLabelGenerator(this::formatRoomStatus);
        statusCombo.setValue(room.getRoomStatus());
        statusCombo.setWidthFull();

        Button saveBtn = new Button("Zapisz", e -> {
            try {
                roomService.updateStatus(room.getId(), statusCombo.getValue());
                loadRoomsFromDatabase();
                dialog.close();
                showSuccess("Status zmieniony na: " + formatRoomStatus(statusCombo.getValue()));
            } catch (Exception ex) {
                showError("Błąd: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(statusCombo);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openAddTaskForSelectedRoomsDialog() {
        if (selectedRooms.isEmpty()) {
            showError("Nie wybrano żadnych pokoi");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle(
                "Dodaj zadanie dla zaznaczonych pokoi (" + selectedRooms.size() + ")"
        );

        FormLayout form = new FormLayout();

        String roomNumbers = selectedRooms.stream()
                .map(Room::getNumber)
                .sorted()
                .collect(Collectors.joining(", "));
        Span roomsInfo = new Span("Pokoje: " + roomNumbers);
        roomsInfo.getStyle().set("font-weight", "bold");

        TextArea descriptionArea = new TextArea("Opis zadania");
        descriptionArea.setRequired(true);
        descriptionArea.setWidthFull();

        TextArea remarksArea = new TextArea("Uwagi");
        remarksArea.setWidthFull();

        DateTimePicker scheduledAtPicker = new DateTimePicker("Zaplanowane na");
        scheduledAtPicker.setValue(LocalDateTime.now());
        scheduledAtPicker.setRequiredIndicatorVisible(true);

        IntegerField durationField = new IntegerField("Czas trwania (minuty)");
        durationField.setValue(30);
        durationField.setMin(1);

        ComboBox<User> assignToCombo = new ComboBox<>("Przypisz do");
        assignToCombo.setItems(
                userService.findAll().stream()
                        .filter(u -> u.getRole() == Role.CLEANER || u.getRole() == Role.MANAGER)
                        .collect(Collectors.toList())
        );
        assignToCombo.setItemLabelGenerator(
                u -> u.getFirstName() + " " + u.getLastName() + " (" + u.getRole() + ")"
        );

        form.add(
                descriptionArea,
                remarksArea,
                scheduledAtPicker,
                durationField,
                assignToCombo
        );

        Button saveBtn = new Button("Dodaj zadania", e -> {

            if (descriptionArea.isEmpty() || scheduledAtPicker.isEmpty()) {
                showError("Opis i data są wymagane");
                return;
            }

            LocalDateTime scheduledAt = scheduledAtPicker.getValue();

            List<Integer> roomIds = selectedRooms.stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());

            if (!taskService.canCreateTasksForRoomsAndDate(
                    roomIds, scheduledAt.toLocalDate())) {
                showError("Niektóre pokoje mają już zadania na ten dzień");
                return;
            }

            try {
                List<Task> tasks = new ArrayList<>();

                for (Room room : selectedRooms) {
                    Task task = new Task();
                    task.setDescription(descriptionArea.getValue());
                    task.setRemarks(remarksArea.getValue());
                    task.setStatus(TaskStatus.PENDING);
                    task.setScheduledAt(scheduledAt);
                    task.setDurationInMinutes(durationField.getValue());
                    task.setAssignedTo(assignToCombo.getValue());
                    task.setRoom(room);

                    tasks.add(task);
                }

                List<Task> created = taskService.createBatch(tasks);

                Set<Integer> affectedRooms = selectedRooms.stream()
                        .map(Room::getId)
                        .collect(Collectors.toSet());

                selectedRooms.clear();
                dialog.close();
                showSuccess("Utworzono " + created.size() + " zadań");

                refreshTasksForRooms(affectedRooms);

            } catch (Exception ex) {
                log.error("Błąd tworzenia zadań", ex);
                showError("Błąd: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(roomsInfo, form));
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }


    private void deleteRoom(Room room) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Potwierdź usunięcie");
        confirmDialog.add(new Span("Czy na pewno chcesz usunąć pokój " + room.getNumber() + "?"));

        Button confirmBtn = new Button("Usuń", e -> {
            try {
                roomService.deleteById(room.getId());
                loadRoomsFromDatabase();
                confirmDialog.close();
                showSuccess("Pokój usunięty");
            } catch (Exception ex) {
                showError("Błąd: " + ex.getMessage());
            }
        });
        confirmBtn.getStyle().set("color", "red");

        Button cancelBtn = new Button("Anuluj", e -> confirmDialog.close());

        confirmDialog.getFooter().add(cancelBtn, confirmBtn);
        confirmDialog.open();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
