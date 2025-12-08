package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.systemhotelowy.model.*;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;
import org.systemhotelowy.utils.NotificationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskBatchDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(TaskBatchDialog.class);

    private final TaskService taskService;
    private final UserService userService;
    private final Set<Room> targetRooms;
    private final User currentUser;
    private final Runnable onSuccess;

    private TextArea descriptionArea;
    private TextArea remarksArea;
    private DateTimePicker scheduledAtPicker;
    private IntegerField durationField;
    private ComboBox<User> assignToCombo;

    public TaskBatchDialog(Set<Room> targetRooms,
                           User currentUser,
                           TaskService taskService,
                           UserService userService,
                           Runnable onSuccess) {
        this.targetRooms = targetRooms;
        this.currentUser = currentUser;
        this.taskService = taskService;
        this.userService = userService;
        this.onSuccess = onSuccess;

        initUI();
    }

    private void initUI() {
        setWidth("600px");
        setHeaderTitle("Dodaj zadanie dla zaznaczonych pokoi (" + targetRooms.size() + ")");

        FormLayout form = createForm();

        String roomNumbers = targetRooms.stream()
                .map(Room::getNumber)
                .sorted()
                .collect(Collectors.joining(", "));
        Span roomsInfo = new Span("Pokoje: " + roomNumbers);
        roomsInfo.getStyle().set("font-weight", "bold");
        roomsInfo.getStyle().set("margin-bottom", "1em");
        roomsInfo.getStyle().set("display", "block");

        Button saveBtn = new Button("Dodaj zadania", e -> saveTasks());
        saveBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Anuluj", e -> this.close());

        add(new VerticalLayout(roomsInfo, form));
        getFooter().add(cancelBtn, saveBtn);
    }

    private FormLayout createForm() {
        FormLayout form = new FormLayout();

        descriptionArea = new TextArea("Opis zadania");
        descriptionArea.setRequired(true);
        descriptionArea.setWidthFull();

        remarksArea = new TextArea("Uwagi");
        remarksArea.setWidthFull();

        scheduledAtPicker = new DateTimePicker("Zaplanowane na");
        scheduledAtPicker.setValue(LocalDateTime.now());
        scheduledAtPicker.setRequiredIndicatorVisible(true);

        durationField = new IntegerField("Czas trwania (minuty)");
        durationField.setValue(30);
        durationField.setMin(1);

        assignToCombo = new ComboBox<>("Przypisz do");
        assignToCombo.setRequired(true);
        List<User> eligibleStaff = userService.findAll().stream()
                .filter(u -> u.getRole() == Role.CLEANER)
                .collect(Collectors.toList());

        assignToCombo.setItems(eligibleStaff);
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
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return form;
    }

    private void saveTasks() {
        if (descriptionArea.isEmpty() || scheduledAtPicker.isEmpty() || assignToCombo.isEmpty()) {
            NotificationUtils.showError("Opis, data i osoba przypisana są wymagane");
            return;
        }

        LocalDateTime scheduledAt = scheduledAtPicker.getValue();
        List<Integer> roomIds = targetRooms.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

        if (!taskService.canCreateTasksForRoomsAndDate(roomIds, scheduledAt.toLocalDate())) {
            NotificationUtils.showError("Niektóre pokoje mają już zadania na ten dzień");
            return;
        }

        try {
            List<Task> tasksToCreate = prepareTasksList(scheduledAt);
            List<Task> created = taskService.createBatch(tasksToCreate);

            NotificationUtils.showSuccess("Utworzono " + created.size() + " zadań");

            if (onSuccess != null) {
                onSuccess.run();
            }
            this.close();

        } catch (Exception ex) {
            log.error("Błąd tworzenia zadań", ex);
            NotificationUtils.showError("Błąd: " + ex.getMessage());
        }
    }

    private List<Task> prepareTasksList(LocalDateTime scheduledAt) {
        List<Task> tasks = new ArrayList<>();
        User assignee = assignToCombo.getValue();
        String description = descriptionArea.getValue();
        String remarks = remarksArea.getValue();
        int duration = durationField.getValue() != null ? durationField.getValue() : 30;

        for (Room room : targetRooms) {
            Task task = new Task();
            task.setDescription(description);
            task.setRemarks(remarks);
            task.setStatus(TaskStatus.PENDING);
            task.setScheduledAt(scheduledAt);
            task.setDurationInMinutes(duration);
            task.setAssignedTo(assignee);
            task.setRoom(room);

            task.setRequestedBy(currentUser);

            tasks.add(task);
        }
        return tasks;
    }
}