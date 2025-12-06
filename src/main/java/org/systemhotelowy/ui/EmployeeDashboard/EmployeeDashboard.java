package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.VaadinAuthenticationService;
import org.systemhotelowy.ui.components.DashboardTopBar;
import org.systemhotelowy.utils.VaadinSecurityHelper;

/**
 * Dashboard dla Pracownika - dostępny tylko dla użytkowników z rolą CLEANER.
 */
@Route("employee")
@PageTitle("Panel Pracownika")
@RolesAllowed("CLEANER")
public class EmployeeDashboard extends VerticalLayout {

    private final VaadinAuthenticationService authService;
    private final VaadinSecurityHelper securityHelper;
    private final RoomService roomService;
    private final TaskService taskService;

    public EmployeeDashboard(
            VaadinAuthenticationService authService,
            VaadinSecurityHelper securityHelper,
            RoomService roomService,
            TaskService taskService
    ) {
        this.authService = authService;
        this.securityHelper = securityHelper;
        this.roomService = roomService;
        this.taskService = taskService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK + KPI
        // =========================
        DashboardTopBar topBar = new DashboardTopBar("Panel Pracownika", authService, securityHelper);
        org.systemhotelowy.ui.EmployeeDashboard.KpiPanel kpiPanel = new KpiPanel();

        add(topBar, kpiPanel);

        // =========================
        //       PANEL POKOI
        // =========================
        RoomPanel roomPanel = new RoomPanel();
        add(roomPanel);

        // =========================
        //       SIATKA REZERWACJI
        // =========================

        // Rozciąganie paneli
        setFlexGrow(0, topBar);       // top bar nie rośnie
        setFlexGrow(0, kpiPanel);     // KPI nie rośnie
        setFlexGrow(1, roomPanel);    // roomPanel rośnie
    }
}
