package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TopBar extends VerticalLayout {

    public TopBar() {
        setWidthFull();
        setPadding(false);
        setMargin(false);

        H1 title = new H1("Panel Kierownika");
        Span loggedUser = new Span("Zalogowano jako: kierownik@hotel.pl");
        Button logoutBtn = new Button("Wyloguj", e -> Notification.show("Wylogowano!"));

        HorizontalLayout rightSide = new HorizontalLayout(loggedUser, logoutBtn);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout topBar = new HorizontalLayout(title, rightSide);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        add(topBar);
    }
}
