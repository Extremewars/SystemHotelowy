package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.systemhotelowy.dto.report.HotelReportImportSummaryDto;
import org.systemhotelowy.service.*;
import org.systemhotelowy.ui.components.DashboardTopBar;
import org.systemhotelowy.utils.VaadinSecurityHelper;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

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
    private final DashboardService dashboardService;
    private final ReportExportService reportExportService;

    public ManagerDashboard(
            VaadinAuthenticationService authService,
            VaadinSecurityHelper securityHelper,
            RoomService roomService,
            TaskService taskService,
            UserService userService,
            ReservationService reservationService,
            DashboardService dashboardService,
            ReportExportService reportExportService
    ) {
        this.authService = authService;
        this.securityHelper = securityHelper;
        this.roomService = roomService;
        this.taskService = taskService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.dashboardService = dashboardService;
        this.reportExportService = reportExportService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK + KPI
        // =========================
        DashboardTopBar topBar = new DashboardTopBar("Panel Kierownika", authService, securityHelper);
        KpiPanel kpiPanel = new KpiPanel(dashboardService);

        add(topBar, kpiPanel);

        // =========================
        //       RAPORTY
        // =========================
        add(createReportSection());

        // =========================
        //       PANEL POKOI
        // =========================
        RoomPanel roomPanel = new RoomPanel(roomService, taskService, userService, authService);
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

    private Component createReportSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Export
        Button exportButton = new Button("Eksportuj Raport Dzienny (XML)");
        String filename = "hotel-report-" + LocalDate.now() + ".xml";
        StreamResource resource = new StreamResource(filename,
                () -> new ByteArrayInputStream(reportExportService.exportDailyReport(LocalDate.now())));

        Anchor exportAnchor = new Anchor(resource, "");
        exportAnchor.getElement().setAttribute("download", true);
        exportAnchor.add(exportButton);

        // Import
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/xml", ".xml");
        upload.setUploadButton(new Button("Importuj Raport (XML)"));
        upload.addSucceededListener(event -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                HotelReportImportSummaryDto summary = reportExportService.importDailyReport(bytes);
                Notification.show("Zaimportowano raport z daty: " + summary.getDate() +
                        ". Pokoje: " + summary.getRoomsCount() +
                        ", Zadania: " + summary.getTasksCount());
            } catch (Exception e) {
                Notification.show("Błąd importu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        layout.add(exportAnchor, upload);
        return layout;
    }
}
