package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Panel KPI - wyświetla statystyki z prawdziwych danych z bazy.
 * Automatycznie odświeża się co 5 sekund.
 */
public class KpiPanel extends VerticalLayout {

    private final RoomService roomService;
    private final TaskService taskService;
    
    private HorizontalLayout kpiLayout;
    private Span readyValue;
    private Span dirtyValue;
    private Span outOfOrderValue;
    private Span tasksValue;
    
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshTask;

    public KpiPanel(RoomService roomService, TaskService taskService) {
        this.roomService = roomService;
        this.taskService = taskService;
        
        setWidthFull();
        setPadding(false);
        setMargin(false);

        kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        kpiLayout.add(createKpiBox("Gotowe", VaadinIcon.CHECK_CIRCLE, value -> readyValue = value));
        kpiLayout.add(createKpiBox("Do sprzątania", VaadinIcon.TRASH, value -> dirtyValue = value));
        kpiLayout.add(createKpiBox("Awaria", VaadinIcon.WARNING, value -> outOfOrderValue = value));
        kpiLayout.add(createKpiBox("Zadania", VaadinIcon.TASKS, value -> tasksValue = value));

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
        long outOfOrderRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.OUT_OF_ORDER)
                .count();
        long totalTasks = taskService.findAll().size();
        
        // Zaktualizuj wartości
        if (readyValue != null) readyValue.setText(String.valueOf(readyRooms));
        if (dirtyValue != null) dirtyValue.setText(String.valueOf(dirtyRooms));
        if (outOfOrderValue != null) outOfOrderValue.setText(String.valueOf(outOfOrderRooms));
        if (tasksValue != null) tasksValue.setText(String.valueOf(totalTasks));
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
