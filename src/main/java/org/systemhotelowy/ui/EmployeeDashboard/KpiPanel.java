package org.systemhotelowy.ui.EmployeeDashboard;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class KpiPanel extends VerticalLayout {

    public KpiPanel() {
        setWidthFull();
        setPadding(false);
        setMargin(false);

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);

        kpiLayout.add(createKpiBox("Wolne", 10, VaadinIcon.CHECK_CIRCLE));
        kpiLayout.add(createKpiBox("Zajęte", 5, VaadinIcon.USER));
        kpiLayout.add(createKpiBox("Zadania", 1, VaadinIcon.NEWSPAPER));
        kpiLayout.add(createKpiBox("Zgłoszenia", 1, VaadinIcon.NEWSPAPER));

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
