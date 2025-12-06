package org.systemhotelowy.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.systemhotelowy.utils.VaadinSecurityHelper;

/**
 * Strona główna - automatycznie przekierowuje zalogowanych użytkowników do odpowiedniego dashboardu.
 */
@Route("")
@AnonymousAllowed
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private final VaadinSecurityHelper securityHelper;

    public MainView(VaadinSecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H1("Witamy w Systemie Hotelowym"));
        add(new Paragraph("Przekierowywanie..."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Automatyczne przekierowanie do dashboardu lub logowania
        securityHelper.navigateToDashboard();
    }
}
