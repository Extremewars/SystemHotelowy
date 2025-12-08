package org.systemhotelowy.service;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final Logger log = LoggerFactory.getLogger(VaadinAuthenticationService.class);
    
    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    public VaadinAuthenticationService(AuthenticationContext authenticationContext, UserService userService) {
        this.authenticationContext = authenticationContext;
        this.userService = userService;
    }

    /**
     * Pobiera aktualnie zalogowanego użytkownika.
     * Próbuje po kolei przez: cache w VaadinSession, AuthenticationContext, SecurityContextHolder.
     */
    public Optional<User> getAuthenticatedUser() {
        // Próba 0: Cache w VaadinSession (najszybsza)
        if (VaadinSession.getCurrent() != null) {
            User cachedUser = VaadinSession.getCurrent().getAttribute(User.class);
            if (cachedUser != null) {
                log.debug("Zwrócono użytkownika z cache VaadinSession: {}", cachedUser.getEmail());
                return Optional.of(cachedUser);
            }
        }
        
        // Próba 1: Użyj AuthenticationContext z Vaadin (preferowana metoda)
        Optional<User> user = authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> {
                    log.debug("Znaleziono użytkownika przez AuthenticationContext: {}", userDetails.getUsername());
                    return userService.findByEmail(userDetails.getUsername());
                });
        
        if (user.isPresent()) {
            log.debug("Zwrócono użytkownika z AuthenticationContext: {}", user.get().getEmail());
            // Cache w sesji dla przyszłych wywołań
            if (VaadinSession.getCurrent() != null) {
                VaadinSession.getCurrent().setAttribute(User.class, user.get());
            }
            return user;
        }
        
        log.debug("AuthenticationContext nie zwrócił użytkownika, próba przez SecurityContextHolder...");
        
        // Próba 2: Użyj SecurityContextHolder ze Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())
                && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Znaleziono użytkownika przez SecurityContextHolder: {}", userDetails.getUsername());
            Optional<User> secUser = userService.findByEmail(userDetails.getUsername());
            if (secUser.isPresent()) {
                log.debug("Zwrócono użytkownika z SecurityContextHolder: {}", secUser.get().getEmail());
                // Cache w sesji dla przyszłych wywołań
                if (VaadinSession.getCurrent() != null) {
                    VaadinSession.getCurrent().setAttribute(User.class, secUser.get());
                }
                return secUser;
            }
        } else if (authentication != null) {
            log.debug("Authentication present ale: isAuthenticated={}, principal={}", 
                    authentication.isAuthenticated(), 
                    authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null");
        }
        
        log.warn("Nie znaleziono zalogowanego użytkownika w żadnym kontekście");
        log.warn("AuthenticationContext: {}", authenticationContext != null ? "dostępny" : "null");
        log.warn("SecurityContextHolder auth: {}", authentication != null ? "dostępny" : "null");
        log.warn("VaadinSession: {}", VaadinSession.getCurrent() != null ? "dostępna" : "null");
        
        return Optional.empty();
    }

    /**
     * Wylogowuje użytkownika.
     */
    public void logout() {
        // Wyczyść cache w sesji
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(User.class, null);
        }
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
