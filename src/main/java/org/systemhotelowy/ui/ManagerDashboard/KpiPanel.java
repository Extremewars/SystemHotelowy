package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;

/**
 * Panel KPI - wyświetla statystyki z prawdziwych danych z bazy.
 */
public class KpiPanel extends VerticalLayout {

    private final RoomService roomService;
    private final TaskService taskService;

    public KpiPanel(RoomService roomService, TaskService taskService) {
        this.roomService = roomService;
        this.taskService = taskService;
        
        setWidthFull();
        setPadding(false);
        setMargin(false);

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        // Obliczanie statystyk z bazy danych
        long totalRooms = roomService.countRooms();
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

        kpiLayout.add(createKpiBox("Gotowe", (int) readyRooms, VaadinIcon.CHECK_CIRCLE));
        kpiLayout.add(createKpiBox("Do sprzątania", (int) dirtyRooms, VaadinIcon.TRASH));
        kpiLayout.add(createKpiBox("Awaria", (int) outOfOrderRooms, VaadinIcon.WARNING));
        kpiLayout.add(createKpiBox("Zadania", (int) totalTasks, VaadinIcon.TASKS));

        add(kpiLayout);
    }

    private Div createKpiBox(String title, int value, VaadinIcon iconType) {
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
        Span v = new Span(String.valueOf(value));
        v.getStyle().set("font-weight", "bold");

        box.add(icon, t, v);
        return box;
    }
}
