package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.VaadinAuthenticationService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel zadań dla pracownika - wyświetla zadania przypisane do zalogowanego użytkownika.
 */
public class TaskPanel extends VerticalLayout {

    private final TaskService taskService;
    private final VaadinAuthenticationService authService;
    
    private Grid<Task> taskGrid;
    private List<Task> myTasks = new ArrayList<>();

    public TaskPanel(TaskService taskService, VaadinAuthenticationService authService) {
        this.taskService = taskService;
        this.authService = authService;
        
        setWidthFull();
        setSpacing(true);

        // ============================
        // FILTRY
        // ============================
        ComboBox<TaskStatus> statusFilter = new ComboBox<>("Filtruj po statusie");
        statusFilter.setItems(TaskStatus.values());
        statusFilter.setItemLabelGenerator(this::formatTaskStatus);
        statusFilter.addValueChangeListener(e -> filterTasks(e.getValue()));

        TextField searchField = new TextField("Szukaj");
        searchField.setPlaceholder("Opis zadania...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterTasks(statusFilter.getValue(), e.getValue()));

        Button refreshBtn = new Button("Odśwież", e -> {
            loadMyTasks();
            Notification.show("Zadania odświeżone", 2000, Notification.Position.BOTTOM_START);
        });

        HorizontalLayout topBar = new HorizontalLayout(statusFilter, searchField, refreshBtn);
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);

        // ============================
        // GRID ZADAŃ
        // ============================
        taskGrid = new Grid<>(Task.class, false);
        taskGrid.setWidthFull();

        // Kolumna Pokój
        taskGrid.addColumn(task -> task.getRoom() != null ? task.getRoom().getNumber() : "-")
                .setHeader("Pokój")
                .setWidth("100px")
                .setFlexGrow(0)
                .setSortable(true);

        // Kolumna Opis
        taskGrid.addColumn(Task::getDescription)
                .setHeader("Opis zadania")
                .setWidth("300px")
                .setFlexGrow(1);

        // Kolumna Status
        taskGrid.addComponentColumn(task -> {
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
        taskGrid.addColumn(task -> task.getScheduledAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .setHeader("Zaplanowane na")
                .setWidth("150px")
                .setFlexGrow(0);

        // Kolumna Czas trwania
        taskGrid.addColumn(task -> task.getDurationInMinutes() + " min")
                .setHeader("Czas")
                .setWidth("80px")
                .setFlexGrow(0);

        // Kolumna Akcje
        taskGrid.addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button detailsBtn = new Button("Szczegóły", e -> openTaskDetailsDialog(task));
            
            Button statusBtn = new Button("Zmień status", e -> openChangeStatusDialog(task));
            statusBtn.setEnabled(task.getStatus() != TaskStatus.DONE && task.getStatus() != TaskStatus.CANCELLED);
            
            actions.add(detailsBtn, statusBtn);
            return actions;
        }).setHeader("Akcje")
                .setWidth("250px")
                .setFlexGrow(0);

        // Style
        taskGrid.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");
        taskGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(topBar, taskGrid);
        
        // Załaduj zadania użytkownika (MUSI BYĆ PO inicjalizacji taskGrid!)
        loadMyTasks();
    }

    /**
     * Publiczna metoda do odświeżania danych z bazy - używana przez automatyczne odświeżanie.
     */
    public void refreshData() {
        loadMyTasks();
    }

    private void loadMyTasks() {
        User currentUser = authService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            myTasks = taskService.findByAssignedToId(currentUser.getId());
            taskGrid.setItems(myTasks);
        }
    }

    private void filterTasks(TaskStatus status) {
        filterTasks(status, null);
    }

    private void filterTasks(TaskStatus status, String searchText) {
        List<Task> filtered = myTasks;
        
        if (status != null) {
            filtered = filtered.stream()
                    .filter(task -> task.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(task -> task.getDescription().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }
        
        taskGrid.setItems(filtered);
    }

    private String formatTaskStatus(TaskStatus status) {
        switch (status) {
            case PENDING: return "Oczekujące";
            case IN_PROGRESS: return "W trakcie";
            case DONE: return "Wykonane";
            case CANCELLED: return "Anulowane";
            default: return status.name();
        }
    }

    private void openTaskDetailsDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Szczegóły zadania");

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(new Span(task.getDescription()), "Opis");
        formLayout.addFormItem(new Span(formatTaskStatus(task.getStatus())), "Status");
        
        if (task.getRemarks() != null && !task.getRemarks().isEmpty()) {
            formLayout.addFormItem(new Span(task.getRemarks()), "Uwagi");
        }
        
        if (task.getRoom() != null) {
            formLayout.addFormItem(new Span(task.getRoom().getNumber()), "Pokój");
        }
        
        formLayout.addFormItem(
            new Span(task.getScheduledAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))), 
            "Zaplanowane na"
        );
        
        formLayout.addFormItem(new Span(task.getDurationInMinutes() + " minut"), "Czas trwania");

        Button closeBtn = new Button("Zamknij", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private void openChangeStatusDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setHeaderTitle("Zmień status zadania");

        ComboBox<TaskStatus> statusCombo = new ComboBox<>("Nowy status");
        statusCombo.setItems(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.DONE, TaskStatus.CANCELLED);
        statusCombo.setItemLabelGenerator(this::formatTaskStatus);
        statusCombo.setValue(task.getStatus());
        statusCombo.setWidthFull();

        Button saveBtn = new Button("Zapisz", e -> {
            try {
                task.setStatus(statusCombo.getValue());
                taskService.update(task);
                loadMyTasks();
                dialog.close();
                showSuccess("Status zmieniony na: " + formatTaskStatus(statusCombo.getValue()));
            } catch (Exception ex) {
                showError("Błąd: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(statusCombo);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
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
