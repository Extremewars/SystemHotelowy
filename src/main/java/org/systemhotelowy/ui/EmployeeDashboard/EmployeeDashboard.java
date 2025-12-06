package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.systemhotelowy.ui.EmployeeDashboard.KpiPanel;
import org.systemhotelowy.ui.EmployeeDashboard.RoomPanel;
import org.systemhotelowy.ui.EmployeeDashboard.TopBar;



@Route("employee")
@PageTitle("Panel Pracownika")
public class EmployeeDashboard extends VerticalLayout {

    public EmployeeDashboard() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK + KPI
        // =========================
        org.systemhotelowy.ui.EmployeeDashboard.TopBar topBar = new TopBar();
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
