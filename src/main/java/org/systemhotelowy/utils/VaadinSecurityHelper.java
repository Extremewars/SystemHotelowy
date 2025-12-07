package org.systemhotelowy.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.systemhotelowy.service.VaadinAuthenticationService;

/**
 * Klasa pomocnicza dla operacji security w komponentach Vaadin.
 */
@Component
public class VaadinSecurityHelper {

    private final VaadinAuthenticationService authService;

    public VaadinSecurityHelper(VaadinAuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Wylogowuje użytkownika i przekierowuje do strony logowania.
     */
    public void logout() {
        // Najpierw wyloguj, potem przekieruj
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
        
        // Przekierowanie po wylogowaniu
        UI.getCurrent().getPage().setLocation("/login");
    }

    /**
     * Przekierowuje do odpowiedniego dashboardu na podstawie roli użytkownika.
     * Jeśli użytkownik nie jest zalogowany, przekierowuje do strony logowania.
     */
    public void navigateToDashboard() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        
        authService.getUserRole().ifPresentOrElse(
                role -> {
                    switch (role) {
                        case MANAGER, ADMIN -> ui.navigate("manager");
                        case CLEANER -> ui.navigate("employee");
                        default -> ui.navigate("login");
                    }
                },
                () -> ui.navigate("login")
        );
    }
}
