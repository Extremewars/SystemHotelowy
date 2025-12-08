package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.TaskStatus;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TaskGrid extends Grid<Task> {

    private final Consumer<Task> onDetailsClick;
    private final Consumer<Task> onStatusClick;

    public TaskGrid(Consumer<Task> onDetailsClick, Consumer<Task> onStatusClick) {
        super(Task.class, false);
        this.onDetailsClick = onDetailsClick;
        this.onStatusClick = onStatusClick;

        configureGrid();
    }

    private void configureGrid() {
        setWidthFull();

        // Kolumna Pokój
        addColumn(task -> task.getRoom() != null ? task.getRoom().getNumber() : "-")
                .setHeader("Pokój")
                .setWidth("100px")
                .setFlexGrow(0)
                .setSortable(true);

        // Kolumna Opis
        addColumn(Task::getDescription)
                .setHeader("Opis zadania")
                .setWidth("260px")
                .setFlexGrow(1);

        // Kolumna Status
        addComponentColumn(task -> {
            Span s = new Span(formatTaskStatus(task.getStatus()));
            s.getStyle().set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-weight", "500");
            
            switch (task.getStatus()) {
                case DONE -> s.getStyle().set("background-color", "#d4edda").set("color", "#155724");
                case IN_PROGRESS -> s.getStyle().set("background-color", "#cce5ff").set("color", "#004085");
                case PENDING -> s.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
                case CANCELLED -> s.getStyle().set("background-color", "#f8d7da").set("color", "#721c24");
            }
            return s;
        }).setHeader("Status")
                .setWidth("140px")
                .setFlexGrow(0);

        // Kolumna Data
        addColumn(task -> task.getScheduledAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .setHeader("Zaplanowane na")
                .setWidth("160px")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Task::getScheduledAt);

        // Kolumna Czas trwania
        addColumn(task -> task.getDurationInMinutes() + " min")
                .setHeader("Czas")
                .setWidth("100px")
                .setFlexGrow(0);

        // Kolumna Akcje
        addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button detailsBtn = new Button("Szczegóły", e -> onDetailsClick.accept(task));
            
            Button statusBtn = new Button("Zmień status", e -> onStatusClick.accept(task));
            statusBtn.setEnabled(task.getStatus() != TaskStatus.DONE && task.getStatus() != TaskStatus.CANCELLED);
            
            actions.add(detailsBtn, statusBtn);
            return actions;
        }).setHeader("Akcje")
                .setWidth("250px")
                .setFlexGrow(0);

        // Style
        getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private String formatTaskStatus(TaskStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "Oczekujące";
            case IN_PROGRESS -> "W trakcie";
            case DONE -> "Wykonane";
            case CANCELLED -> "Anulowane";
            default -> status.name();
        };
    }
}
