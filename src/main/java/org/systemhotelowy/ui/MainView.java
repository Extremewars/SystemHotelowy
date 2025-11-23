package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        add(new H1("Witamy w Systemie Hotelowym"));

        Button btn = new Button("Kliknij mnie");
        btn.addClickListener(e -> btn.setText("Kliknięto!"));

        add(btn);
    }
}
