package org.systemhotelowy.ui.EmployeeDashboard;

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

/**
 * Panel KPI dla pracownika - statystyki zadań i pokoi.
 */
public class KpiPanel extends VerticalLayout {

    private final RoomService roomService;
    private final TaskService taskService;
    private final VaadinAuthenticationService authService;

    public KpiPanel(RoomService roomService, TaskService taskService, VaadinAuthenticationService authService) {
        this.roomService = roomService;
        this.taskService = taskService;
        this.authService = authService;
        
        setWidthFull();
        setPadding(false);
        setMargin(false);

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        // Statystyki dla zalogowanego pracownika
        User currentUser = authService.getAuthenticatedUser().orElse(null);
        
        long readyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.READY)
                .count();
        long dirtyRooms = roomService.findAll().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.DIRTY)
                .count();
        
        long myTasks = 0;
        long myPendingTasks = 0;
        if (currentUser != null) {
            myTasks = taskService.findByAssignedToId(currentUser.getId()).size();
            myPendingTasks = taskService.findByAssignedToId(currentUser.getId()).stream()
                    .filter(task -> task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
        }

        kpiLayout.add(createKpiBox("Gotowe pokoje", (int) readyRooms, VaadinIcon.CHECK_CIRCLE));
        kpiLayout.add(createKpiBox("Do sprzątania", (int) dirtyRooms, VaadinIcon.TRASH));
        kpiLayout.add(createKpiBox("Moje zadania", (int) myTasks, VaadinIcon.TASKS));
        kpiLayout.add(createKpiBox("Do wykonania", (int) myPendingTasks, VaadinIcon.CLOCK));

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
