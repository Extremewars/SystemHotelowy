package org.systemhotelowy.service;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.systemhotelowy.model.Role;
import org.systemhotelowy.model.User;

import java.util.Optional;

/**
 * Serwis do zarządzania autentykacją w kontekście Vaadin.
 * Obsługuje logowanie, wylogowanie i sprawdzanie ról użytkownika.
 */
@Service
public class VaadinAuthenticationService {

    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    public VaadinAuthenticationService(AuthenticationContext authenticationContext, UserService userService) {
        this.authenticationContext = authenticationContext;
        this.userService = userService;
    }

    /**
     * Pobiera aktualnie zalogowanego użytkownika.
     */
    public Optional<User> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> userService.findByEmail(userDetails.getUsername()));
    }

    /**
     * Wylogowuje użytkownika.
     */
    public void logout() {
        authenticationContext.logout();
    }

    /**
     * Sprawdza czy użytkownik ma określoną rolę.
     */
    public boolean hasRole(Role role) {
        return getAuthenticatedUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }

    /**
     * Pobiera rolę zalogowanego użytkownika.
     */
    public Optional<Role> getUserRole() {
        return getAuthenticatedUser().map(User::getRole);
    }

    /**
     * Zapisuje dodatkowe dane w sesji Vaadin.
     */
    public void setSessionAttribute(String key, Object value) {
        VaadinSession.getCurrent().setAttribute(key, value);
    }

    /**
     * Pobiera dane z sesji Vaadin.
     */
    public <T> T getSessionAttribute(String key, Class<T> type) {
        return VaadinSession.getCurrent().getAttribute(type);
    }
}
