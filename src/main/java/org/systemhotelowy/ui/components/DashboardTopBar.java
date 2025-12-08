package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.VaadinAuthenticationService;
import org.systemhotelowy.utils.NotificationUtils;
import org.systemhotelowy.utils.VaadinSecurityHelper;

/**
 * Wspólny komponent górnego paska dla wszystkich dashboardów.
 * Wyświetla tytuł, zalogowanego użytkownika i przycisk wylogowania.
 */
public class DashboardTopBar extends VerticalLayout {

    private final VaadinAuthenticationService authService;
    private final VaadinSecurityHelper securityHelper;

    public DashboardTopBar(String title, VaadinAuthenticationService authService, VaadinSecurityHelper securityHelper) {
        this.authService = authService;
        this.securityHelper = securityHelper;

        setWidthFull();
        setPadding(false);
        setMargin(false);

        H1 titleLabel = new H1(title);
        
        User user = authService.getAuthenticatedUser().orElse(null);
        String userEmail = user != null ? user.getEmail() : "Nieznany użytkownik";
        Span loggedUser = new Span("Zalogowano jako: " + userEmail);
        
        Button logoutBtn = new Button("Wyloguj", e -> {
            securityHelper.logout();
            NotificationUtils.showSuccess("Wylogowano!");
        });

        HorizontalLayout rightSide = new HorizontalLayout(loggedUser, logoutBtn);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout topBar = new HorizontalLayout(titleLabel, rightSide);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        add(topBar);
    }
}
