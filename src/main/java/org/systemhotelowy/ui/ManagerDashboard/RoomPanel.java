package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Importy Spring Security
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;
import org.systemhotelowy.ui.components.RoomFormDialog;
import org.systemhotelowy.ui.components.RoomGrid;
import org.systemhotelowy.ui.components.TaskBatchDialog;

import java.util.Set;
import java.util.Optional;

public class RoomPanel extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(RoomPanel.class);

    private final RoomService roomService;
    private final TaskService taskService;
    private final UserService userService;

    private RoomGrid roomGrid;
    private ComboBox<RoomStatus> statusFilter;
    private TextField searchField;
    private Button addTasksBtn;

    public RoomPanel(RoomService roomService, TaskService taskService, UserService userService) {
        this.roomService = roomService;
        this.taskService = taskService;
        this.userService = userService;

        setWidthFull();
        setSpacing(true);

        createToolbar();
        createGrid();

        refreshGrid();
    }

    private void createToolbar() {
        statusFilter = new ComboBox<>("Status");
        statusFilter.setItems(RoomStatus.values());
        statusFilter.setItemLabelGenerator(this::translateStatus);
        statusFilter.addValueChangeListener(e -> refreshGrid());

        searchField = new TextField("Szukaj pokoju");
        searchField.setPlaceholder("np. 101");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        Button refreshBtn = new Button("Odśwież", e -> {
            refreshGrid();
            showNotification("Dane odświeżone", NotificationVariant.LUMO_SUCCESS);
        });

        Button addRoomBtn = new Button("Dodaj pokój", e -> openRoomDialog(new Room()));

        addTasksBtn = new Button("Dodaj zadania dla zaznaczonych", e -> openBatchTaskDialog());
        addTasksBtn.setEnabled(false);

        HorizontalLayout topBar = new HorizontalLayout(
                statusFilter, searchField, addTasksBtn, refreshBtn, addRoomBtn
        );
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);
        topBar.expand(searchField);

        add(topBar);
    }

    private void createGrid() {
        roomGrid = new RoomGrid(taskService, this::onRoomSelectionChanged);
        roomGrid.addEditListener(this::openRoomDialog);
        roomGrid.addStatusListener(this::openStatusDialog);
        roomGrid.addDeleteListener(this::deleteRoom);
        roomGrid.setTaskUpdateCallback(this::refreshGrid);

        add(roomGrid);
    }

    private void onRoomSelectionChanged(Set<Room> selectedRooms) {
        addTasksBtn.setEnabled(!selectedRooms.isEmpty());
        addTasksBtn.setText("Dodaj zadania (" + selectedRooms.size() + ")");
    }

    private void refreshGrid() {
        RoomStatus status = statusFilter.getValue();
        String search = searchField.getValue();
        var allRooms = roomService.findAll();
        roomGrid.setRooms(allRooms, status, search);
    }

    private void openRoomDialog(Room room) {
        RoomFormDialog dialog = new RoomFormDialog(room, roomService, this::refreshGrid);
        dialog.open();
    }

    private void openStatusDialog(Room room) {
        RoomFormDialog dialog = new RoomFormDialog(room, roomService, this::refreshGrid);
        dialog.openStatusOnlyMode();
    }

    // ========================================================
    // POPRAWIONA METODA OTWIERAJĄCA DIALOG ZBIORCZYCH ZADAŃ
    // ========================================================
    private void openBatchTaskDialog() {
        Set<Room> selected = roomGrid.getSelectedRooms();
        if (selected.isEmpty()) return;

        // 1. Pobierz Authentication z kontekstu
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            showNotification("Musisz być zalogowany, aby dodać zadania", NotificationVariant.LUMO_ERROR);
            return;
        }

        // 2. Pobierz nazwę (email) i znajdź użytkownika w bazie
        String currentEmail = authentication.getName();
        Optional<User> currentUserOpt = userService.findByEmail(currentEmail);

        if (currentUserOpt.isEmpty()) {
            showNotification("Nie znaleziono zalogowanego użytkownika w bazie", NotificationVariant.LUMO_ERROR);
            return;
        }

        User currentUser = currentUserOpt.get();

        TaskBatchDialog dialog = new TaskBatchDialog(
                selected,
                currentUser, // <--- Tutaj przekazujemy obiekt User
                taskService,
                userService,
                () -> {
                    roomGrid.deselectAll();
                    refreshGrid();
                }
        );
        dialog.open();
    }

    private void deleteRoom(Room room) {
        try {
            roomService.deleteById(room.getId());
            refreshGrid();
            showNotification("Pokój usunięty", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Nie można usunąć pokoju: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String translateStatus(RoomStatus status) {
        if (status == null) return "";
        return switch (status) {
            case READY -> "Gotowy";
            case DIRTY -> "Brudny";
            case CLEANING -> "Sprzątanie";
            case IN_MAINTENANCE -> "Konserwacja";
            case OUT_OF_ORDER -> "Awaria";
            default -> status.name();
        };
    }

    private void showNotification(String text, NotificationVariant variant) {
        Notification notification = Notification.show(text, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(variant);
    }
}