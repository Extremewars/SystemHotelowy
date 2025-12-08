package org.systemhotelowy.ui.ManagerDashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.dto.ManagerKpiData;
import org.systemhotelowy.service.DashboardService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Panel KPI - wyświetla statystyki z prawdziwych danych z bazy.
 * Automatycznie odświeża się co 5 sekund.
 */
public class KpiPanel extends VerticalLayout {

    private final DashboardService dashboardService;
    
    private HorizontalLayout kpiLayout;
    private Span readyValue;
    private Span dirtyValue;
    private Span outOfOrderValue;
    private Span tasksValue;
    
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshTask;

    public KpiPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        
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
        ManagerKpiData data = dashboardService.getManagerKpiData();
        
        // Zaktualizuj wartości
        if (readyValue != null) readyValue.setText(String.valueOf(data.getReadyRooms()));
        if (dirtyValue != null) dirtyValue.setText(String.valueOf(data.getDirtyRooms()));
        if (outOfOrderValue != null) outOfOrderValue.setText(String.valueOf(data.getOutOfOrderRooms()));
        if (tasksValue != null) tasksValue.setText(String.valueOf(data.getTotalTasks()));
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
