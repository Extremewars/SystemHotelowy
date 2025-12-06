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
        UI.getCurrent().getPage().setLocation("/login");
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }

    /**
     * Przekierowuje do odpowiedniego dashboardu na podstawie roli użytkownika.
     */
    public void navigateToDashboard() {
        authService.getUserRole().ifPresentOrElse(
                role -> {
                    switch (role) {
                        case MANAGER, ADMIN -> {
                            if (UI.getCurrent() != null) {
                                UI.getCurrent().navigate("manager");
                            }
                        }
                        case CLEANER -> {
                            if (UI.getCurrent() != null) {
                                UI.getCurrent().navigate("employee");
                            }
                        }
                        default -> {
                            if (UI.getCurrent() != null) {
                                UI.getCurrent().navigate("login");
                            }
                        }
                    }
                },
                () -> {
                    if (UI.getCurrent() != null) {
                        UI.getCurrent().navigate("login");
                    }
                }
        );
    }
}
