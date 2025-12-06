package org.systemhotelowy.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.systemhotelowy.ui.LoginView;

/**
 * Konfiguracja bezpieczeństwa specyficzna dla Vaadin.
 * Obsługuje routing Vaadin i sesje po stronie serwera.
 */
@Configuration
@EnableWebSecurity
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Konfiguracja standardowa dla Vaadin
        super.configure(http);
        
        // Ustawienie strony logowania
        setLoginView(http, LoginView.class);
    }
}
