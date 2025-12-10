package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.VaadinAuthenticationService;
import org.systemhotelowy.ui.components.TaskGrid;
import org.systemhotelowy.utils.NotificationUtils;

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
    
    private TaskGrid taskGrid;
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
            NotificationUtils.showSuccess("Zadania odświeżone");
        });

        HorizontalLayout topBar = new HorizontalLayout(statusFilter, searchField, refreshBtn);
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.END);

        // ============================
        // GRID ZADAŃ
        // ============================
        taskGrid = new TaskGrid(this::openTaskDetailsDialog, this::openChangeStatusDialog);

        add(topBar, taskGrid);
        
        // Załaduj zadania użytkownika
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
                NotificationUtils.showSuccess("Status zmieniony na: " + formatTaskStatus(statusCombo.getValue()));
            } catch (Exception ex) {
                NotificationUtils.showError("Błąd: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Anuluj", e -> dialog.close());

        dialog.add(statusCombo);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }
}
