package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("manager")
@PageTitle("Panel Kierownika")
public class ManagerDashboard extends VerticalLayout {

    public ManagerDashboard() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // =========================
        //       GÓRNY PASEK + KPI
        // =========================
        TopBar topBar = new TopBar();
        KpiPanel kpiPanel = new KpiPanel();

        add(topBar, kpiPanel);

        // =========================
        //       PANEL POKOI
        // =========================
        RoomPanel roomPanel = new RoomPanel();
        add(roomPanel);

        // =========================
        //       SIATKA REZERWACJI
        // =========================
        ReservationCalendarGrid reservationGrid = new ReservationCalendarGrid();
        add(reservationGrid);

        // Rozciąganie paneli
        setFlexGrow(0, topBar);       // top bar nie rośnie
        setFlexGrow(0, kpiPanel);     // KPI nie rośnie
        setFlexGrow(1, roomPanel);    // roomPanel rośnie
        setFlexGrow(2, reservationGrid); // reservationGrid rośnie mocniej
    }
}
