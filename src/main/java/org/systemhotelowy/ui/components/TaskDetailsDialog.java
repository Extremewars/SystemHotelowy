package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.utils.NotificationUtils;

/**
 * Dialog wyświetlający szczegóły pojedynczego zadania.
 */
public class TaskDetailsDialog extends Dialog {

    private final TaskService taskService;
    private final Runnable onSuccess;
    private final Task task;

    public TaskDetailsDialog(Task task, TaskService taskService, Runnable onSuccess) {
        this.task = task;
        this.taskService = taskService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Zadanie #" + task.getId());
        setWidth("500px");

        createContent();
        createFooter();
    }

    private void createContent() {
        FormLayout form = new FormLayout();

        // Bezpieczne pobieranie danych (np. obsługa nulli)
        String assignedName = (task.getAssignedTo() != null)
                ? task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName()
                : "Nieprzypisane";

        String roomNumber = (task.getRoom() != null)
                ? task.getRoom().getNumber()
                : "Brak pokoju";

        form.addFormItem(new Span(task.getDescription()), "Opis");
        form.addFormItem(new Span(translateTaskStatus(task.getStatus())), "Status");
        form.addFormItem(new Span(roomNumber), "Pokój");
        form.addFormItem(new Span(assignedName), "Wykonawca");

        if (task.getRemarks() != null && !task.getRemarks().isEmpty()) {
            form.addFormItem(new Span(task.getRemarks()), "Uwagi");
        }

        add(form);
    }


    private void createFooter() {
        Button closeBtn = new Button("Zamknij", e -> close());

        Button deleteBtn = new Button("Usuń zadanie", e -> deleteTask());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        getFooter().add(deleteBtn, closeBtn);
    }

    private void deleteTask() {
        try {
            taskService.deleteById(task.getId());
            NotificationUtils.showSuccess("Zadanie usunięte");
            if (onSuccess != null) onSuccess.run();
            close();
        } catch (Exception e) {
            NotificationUtils.showError("Błąd: " + e.getMessage());
        }
    }

    private String translateTaskStatus(org.systemhotelowy.model.TaskStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "Oczekujące";
            case IN_PROGRESS -> "W trakcie";
            case DONE -> "Zakończone";
            case CANCELLED -> "Anulowane";
            default -> status.name();
        };
    }
}