package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.VaadinAuthenticationService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Panel KPI dla pracownika - statystyki zadań i pokoi.
 * Automatycznie odświeża się co 5 sekund.
 */
public class KpiPanel extends VerticalLayout {

    private final RoomService roomService;
    private final TaskService taskService;
    private final VaadinAuthenticationService authService;
    
    private HorizontalLayout kpiLayout;
    private Span readyValue;
    private Span dirtyValue;
    private Span myTasksValue;
    private Span pendingTasksValue;
    
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshTask;

    public KpiPanel(RoomService roomService, TaskService taskService, VaadinAuthenticationService authService) {
        this.roomService = roomService;
        this.taskService = taskService;
        this.authService = authService;
        
        setWidthFull();
        setPadding(false);
        setMargin(false);

        kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        kpiLayout.add(createKpiBox("Gotowe pokoje", VaadinIcon.CHECK_CIRCLE, value -> readyValue = value));
        kpiLayout.add(createKpiBox("Do sprzątania", VaadinIcon.TRASH, value -> dirtyValue = value));
        kpiLayout.add(createKpiBox("Moje zadania", VaadinIcon.TASKS, value -> myTasksValue = value));
        kpiLayout.add(createKpiBox("Do wykonania", VaadinIcon.CLOCK, value -> pendingTasksValue = value));

        add(kpiLayout);
        
        // Początkowe załadowanie danych
        refreshData();
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Uruchom automatyczne odświeżanie co 5 sekund
        scheduler = Executors.newScheduledThreadPool(1);
        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            attachEvent.getUI().access(this::refreshData);
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        
        // Zatrzymaj odświeżanie gdy komponent jest odłączony
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
    
    private void refreshData() {
        // Pobierz aktualne dane z bazy
        long readyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.READY)
                .count();
        long dirtyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.DIRTY)
                .count();
        
        // Statystyki dla zalogowanego pracownika
        User currentUser = authService.getAuthenticatedUser().orElse(null);
        long myTasks = 0;
        long myPendingTasks = 0;
        if (currentUser != null) {
            myTasks = taskService.findByAssignedToId(currentUser.getId()).size();
            myPendingTasks = taskService.findByAssignedToId(currentUser.getId()).stream()
                    .filter(task -> task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
        }
        
        // Zaktualizuj wartości
        if (readyValue != null) readyValue.setText(String.valueOf(readyRooms));
        if (dirtyValue != null) dirtyValue.setText(String.valueOf(dirtyRooms));
        if (myTasksValue != null) myTasksValue.setText(String.valueOf(myTasks));
        if (pendingTasksValue != null) pendingTasksValue.setText(String.valueOf(myPendingTasks));
    }

    private Div createKpiBox(String title, VaadinIcon iconType, java.util.function.Consumer<Span> valueConsumer) {
        Div box = new Div();
        box.getStyle().set("border", "1px solid #ccc");
        box.getStyle().set("padding", "10px");
        box.getStyle().set("border-radius", "5px");
        box.getStyle().set("text-align", "center");
        box.getStyle().set("flex", "1");

        Icon icon = iconType.create();
        icon.setSize("32px");

        Span t = new Span(title);
        t.getStyle().set("display", "block");
        Span v = new Span("0");
        v.getStyle().set("font-weight", "bold");
        v.getStyle().set("font-size", "24px");
        
        // Przekaż referencję do Span wartości
        valueConsumer.accept(v);

        box.add(icon, t, v);
        return box;
    }
}
