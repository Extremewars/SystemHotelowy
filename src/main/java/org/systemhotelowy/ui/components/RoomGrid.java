package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.RoomType;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.service.TaskService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RoomGrid extends Grid<Room> {

    private final TaskService taskService;
    private final Consumer<Set<Room>> selectionListener;

    // Cache tasków, aby nie "obciążać" bazy w pętli renderera
    private Map<Integer, List<Task>> tasksCache = new HashMap<>();

    // Callbacki do akcji
    private Consumer<Room> onEdit;
    private Consumer<Room> onStatusChange;
    private Consumer<Room> onDelete;
    private Runnable onTaskUpdate;

    public RoomGrid(TaskService taskService, Consumer<Set<Room>> selectionListener) {
        super(Room.class, false);
        this.taskService = taskService;
        this.selectionListener = selectionListener;

        configureGrid();
        addColumns();
    }

    private void configureGrid() {
        setWidthFull();
        setSelectionMode(SelectionMode.MULTI);
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

        asMultiSelect().addSelectionListener(e -> {
            if (selectionListener != null) {
                selectionListener.accept(e.getAllSelectedItems());
            }
        });
    }

    private void addColumns() {
        addColumn(Room::getNumber)
                .setHeader("Numer")
                .setWidth("100px")
                .setSortable(true)
                .setComparator((r1, r2) -> {
                    String n1 = r1.getNumber();
                    String n2 = r2.getNumber();
                    try {
                        return Integer.compare(Integer.parseInt(n1), Integer.parseInt(n2));
                    } catch (NumberFormatException e) {
                        return n1.compareToIgnoreCase(n2);
                    }
                });

        addColumn(r -> r.getFloor() != null ? r.getFloor() : "-").setHeader("Piętro").setWidth("80px");

        addColumn(r -> formatType(r.getType())).setHeader("Typ").setWidth("120px");

        addComponentColumn(this::createStatusBadge).setHeader("Status").setWidth("140px");

        // Kolumna z zadaniami - kluczowa zmiana
        addColumn(new ComponentRenderer<>(this::createTasksLayout))
                .setHeader("Zadania")
                .setWidth("250px")
                .setFlexGrow(1);

        addComponentColumn(this::createActionsLayout).setHeader("Akcje").setWidth("260px");
    }

    // --- ŁADOWANIE DANYCH ---

    public void setRooms(List<Room> allRooms, RoomStatus statusFilter, String searchFilter) {
        // Filtrowanie
        List<Room> filtered = allRooms.stream()
                .filter(r -> statusFilter == null || r.getRoomStatus() == statusFilter)
                .filter(r -> searchFilter == null || searchFilter.isBlank() ||
                        r.getNumber().toLowerCase().contains(searchFilter.trim().toLowerCase()))
                .collect(Collectors.toList());

        // Pobranie tasków dla przefiltrowanych pokoi (Batch fetching)
        tasksCache.clear();
        if (!filtered.isEmpty()) {
            List<Integer> roomIds = filtered.stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());

            List<Task> allTasks = taskService.findByRoomIds(roomIds);

            // Grupujemy taski po ID pokoju
            Map<Integer, List<Task>> groupedTasks = allTasks.stream()
                    .collect(Collectors.groupingBy(t -> t.getRoom().getId()));

            tasksCache.putAll(groupedTasks);
        }

        setItems(filtered);
    }

    public Set<Room> getSelectedRooms() {
        return asMultiSelect().getSelectedItems();
    }

    @Override
    public void deselectAll() {
        asMultiSelect().deselectAll();
    }

    // --- RENDERERY ---

    private VerticalLayout createTasksLayout(Room room) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("gap", "4px");

        List<Task> tasks = tasksCache.getOrDefault(room.getId(), Collections.emptyList());

        if (tasks.isEmpty()) {
            Span empty = new Span("-");
            empty.getStyle().set("color", "#ccc");
            layout.add(empty);
        } else {
            for (Task task : tasks) {
                Span badge = createTaskBadge(task);
                badge.addClickListener(e -> openTaskDetails(task));
                layout.add(badge);
            }
        }
        return layout;
    }

    private Span createTaskBadge(Task task) {
        String desc = task.getDescription();
        if (desc.length() > 25) desc = desc.substring(0, 25) + "...";

        Span badge = new Span(desc);
        badge.getStyle()
                .set("background-color", "#eef")
                .set("color", "#335")
                .set("border", "1px solid #ccd")
                .set("border-radius", "4px")
                .set("padding", "2px 6px")
                .set("font-size", "0.85em")
                .set("cursor", "pointer");
        return badge;
    }

    private void openTaskDetails(Task task) {
        TaskDetailsDialog dialog = new TaskDetailsDialog(task, taskService, () -> {
            if (onTaskUpdate != null) onTaskUpdate.run();
        });
        dialog.open();
    }

    private HorizontalLayout createActionsLayout(Room room) {
        Button editBtn = new Button("Edytuj", e -> {
            if (onEdit != null) onEdit.accept(room);
        });
        Button statusBtn = new Button("Status", e -> {
            if (onStatusChange != null) onStatusChange.accept(room);
        });

        Button deleteBtn = new Button("Usuń", e -> {
            if (onDelete != null) onDelete.accept(room);
        });
        deleteBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(editBtn, statusBtn, deleteBtn);
    }

    private Span createStatusBadge(Room room) {
        String label;
        String color;
        String bg;

        switch (room.getRoomStatus()) {
            case READY -> {
                label = "Gotowy";
                color = "#155724";
                bg = "#d4edda";
            }
            case DIRTY -> {
                label = "Brudny";
                color = "#856404";
                bg = "#fff3cd";
            }
            case CLEANING -> {
                label = "Sprzątanie";
                color = "#004085";
                bg = "#cce5ff";
            }
            case OUT_OF_ORDER, IN_MAINTENANCE -> {
                label = "Niedostępny";
                color = "#721c24";
                bg = "#f8d7da";
            }
            default -> {
                label = room.getRoomStatus().name();
                color = "#333";
                bg = "#eee";
            }
        }

        Span s = new Span(label);
        s.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-weight", "500")
                .set("color", color)
                .set("background-color", bg);
        return s;
    }

    private String formatType(RoomType type) {
        if (type == null) return "";
        return switch (type) {
            case SINGLE -> "Pojedynczy";
            case DOUBLE -> "Podwójny";
            case SUITE -> "Apartament";
            case OTHER -> "Inny";
            default -> type.name();
        };
    }

    // --- SETTERY LISTENERÓW ---

    public void addEditListener(Consumer<Room> listener) {
        this.onEdit = listener;
    }

    public void addStatusListener(Consumer<Room> listener) {
        this.onStatusChange = listener;
    }

    public void addDeleteListener(Consumer<Room> listener) {
        this.onDelete = listener;
    }

    public void setTaskUpdateCallback(Runnable callback) {
        this.onTaskUpdate = callback;
    }
}