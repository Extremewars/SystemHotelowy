package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.systemhotelowy.service.ReservationService;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;
import org.systemhotelowy.service.VaadinAuthenticationService;
import org.systemhotelowy.ui.components.DashboardTopBar;
import org.systemhotelowy.utils.VaadinSecurityHelper;

/**
 * Dashboard dla Kierownika - dostępny tylko dla użytkowników z rolą MANAGER lub ADMIN.
 */
@Route("manager")
@PageTitle("Panel Kierownika")
@RolesAllowed({"MANAGER", "ADMIN"})
public class ManagerDashboard extends VerticalLayout {

    private final VaadinAuthenticationService authService;
    private final VaadinSecurityHelper securityHelper;
    private final RoomService roomService;
    private final TaskService taskService;
    private final UserService userService;
    private final ReservationService reservationService;

    public ManagerDashboard(
            VaadinAuthenticationService authService,
            VaadinSecurityHelper securityHelper,
            RoomService roomService,
            TaskService taskService,
            UserService userService,
            ReservationService reservationService
    ) {
        this.authService = authService;
        this.securityHelper = securityHelper;
        this.roomService = roomService;
        this.taskService = taskService;
        this.userService = userService;
        this.reservationService = reservationService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK + KPI
        // =========================
        DashboardTopBar topBar = new DashboardTopBar("Panel Kierownika", authService, securityHelper);
        KpiPanel kpiPanel = new KpiPanel(roomService, taskService);

        add(topBar, kpiPanel);

        // =========================
        //       PANEL POKOI
        // =========================
        RoomPanel roomPanel = new RoomPanel(roomService, taskService, userService);
        add(roomPanel);

        // =========================
        //       KALENDARZ REZERWACJI
        // =========================
        ReservationCalendar reservationCalendar = new ReservationCalendar(reservationService, roomService);
        add(reservationCalendar);

        // Rozciąganie paneli
        setFlexGrow(0, topBar);       // top bar nie rośnie
        setFlexGrow(0, kpiPanel);     // KPI nie rośnie
        setFlexGrow(1, roomPanel);    // roomPanel rośnie
        setFlexGrow(2, reservationCalendar); // reservationCalendar rośnie mocniej
    }
}
