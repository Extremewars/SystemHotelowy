package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.systemhotelowy.dto.UserRequest;
import org.systemhotelowy.mapper.UserMapper;
import org.systemhotelowy.model.Role;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.UserService;
import org.systemhotelowy.service.VaadinAuthenticationService;
import org.systemhotelowy.utils.NotificationUtils;
import org.systemhotelowy.utils.VaadinSecurityHelper;

/**
 * Widok logowania i rejestracji z prawdziwą integracją Spring Security.
 */
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final VaadinAuthenticationService authService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final VaadinSecurityHelper securityHelper;

    public LoginView(
            VaadinAuthenticationService authService,
            UserService userService,
            UserMapper userMapper,
            VaadinSecurityHelper securityHelper
    ) {
        this.authService = authService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.securityHelper = securityHelper;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // ===========================
        // KARTA LOGOWANIA (zastępstwo za Card)
        // ===========================
        Div loginCard = new Div();
        loginCard.getStyle().set("border", "1px solid #ccc");
        loginCard.getStyle().set("padding", "20px");
        loginCard.getStyle().set("border-radius", "8px");
        loginCard.getStyle().set("width", "400px");
        loginCard.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        VerticalLayout loginLayout = new VerticalLayout();
        loginLayout.setPadding(true);
        loginLayout.setSpacing(true);

        H2 loginTitle = new H2("Logowanie");

        EmailField emailFieldLogin = new EmailField("Email");
        emailFieldLogin.setWidthFull();
        emailFieldLogin.setRequired(true);
        emailFieldLogin.setErrorMessage("Podaj poprawny email");

        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setWidthFull();
        passwordField.setRequired(true);

        Button loginButton = new Button("Zaloguj");
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> handleLogin(emailFieldLogin.getValue(), passwordField.getValue()));

        Span switchToRegister = new Span("Nie masz konta? Zarejestruj się");
        switchToRegister.getStyle().set("cursor", "pointer");
        switchToRegister.getStyle().set("color", "blue");
        switchToRegister.getStyle().set("text-decoration", "underline");

        loginLayout.add(loginTitle, emailFieldLogin, passwordField, loginButton, switchToRegister);
        loginCard.add(loginLayout);

        // ===========================
        // KARTA REJESTRACJI
        // ===========================
        Div registerCard = new Div();
        registerCard.getStyle().set("border", "1px solid #ccc");
        registerCard.getStyle().set("padding", "20px");
        registerCard.getStyle().set("border-radius", "8px");
        registerCard.getStyle().set("width", "400px");
        registerCard.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        registerCard.setVisible(false);

        VerticalLayout registerLayout = new VerticalLayout();
        registerLayout.setPadding(true);
        registerLayout.setSpacing(true);

        H2 registerTitle = new H2("Rejestracja");

        TextField firstNameField = new TextField("Imię");
        firstNameField.setWidthFull();
        firstNameField.setRequired(true);

        TextField lastNameField = new TextField("Nazwisko");
        lastNameField.setWidthFull();
        lastNameField.setRequired(true);

        EmailField emailRegister = new EmailField("Email");
        emailRegister.setWidthFull();
        emailRegister.setRequired(true);

        PasswordField regPassword = new PasswordField("Hasło");
        regPassword.setWidthFull();
        regPassword.setRequired(true);
        regPassword.setHelperText("Minimum 6 znaków");

        PasswordField regConfirmPassword = new PasswordField("Potwierdź hasło");
        regConfirmPassword.setWidthFull();
        regConfirmPassword.setRequired(true);

        RadioButtonGroup<String> roleSelector = new RadioButtonGroup<>();
        roleSelector.setLabel("Rola");
        roleSelector.setItems("Pracownik", "Kierownik");
        roleSelector.setValue("Pracownik");

        Button registerButton = new Button("Zarejestruj");
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> handleRegister(
                firstNameField.getValue(),
                lastNameField.getValue(),
                emailRegister.getValue(),
                regPassword.getValue(),
                regConfirmPassword.getValue(),
                roleSelector.getValue()
        ));

        Span switchToLogin = new Span("Masz już konto? Zaloguj się");
        switchToLogin.getStyle().set("cursor", "pointer");
        switchToLogin.getStyle().set("color", "blue");
        switchToLogin.getStyle().set("text-decoration", "underline");

        registerLayout.add(registerTitle, firstNameField, lastNameField, emailRegister, 
                          regPassword, regConfirmPassword, roleSelector, registerButton, switchToLogin);
        registerCard.add(registerLayout);

        // ===========================
        // PRZEŁĄCZANIE WIDOKÓW
        // ===========================
        switchToRegister.addClickListener(e -> {
            loginCard.setVisible(false);
            registerCard.setVisible(true);
        });

        switchToLogin.addClickListener(e -> {
            registerCard.setVisible(false);
            loginCard.setVisible(true);
        });

        // ===========================
        // DODANIE DO LAYOUTU
        // ===========================
        add(loginCard, registerCard);
    }

    /**
     * Obsługa logowania przez Spring Security i Vaadin.
     */
    private void handleLogin(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            NotificationUtils.showError("Wypełnij wszystkie pola");
            return;
        }

        if (authService.login(email, password)) {
            NotificationUtils.showSuccess("Zalogowano pomyślnie!");
            securityHelper.navigateToDashboard();
        } else {
            NotificationUtils.showError("Nieprawidłowy email lub hasło");
        }
    }

    /**
     * Obsługa rejestracji nowego użytkownika.
     */
    private void handleRegister(String firstName, String lastName, String email, 
                                String password, String confirmPassword, String role) {
        // Walidacja
        if (firstName == null || firstName.isBlank() ||
            lastName == null || lastName.isBlank() ||
            email == null || email.isBlank() ||
            password == null || password.isBlank()) {
            NotificationUtils.showError("Wypełnij wszystkie pola");
            return;
        }

        if (password.length() < 6) {
            NotificationUtils.showError("Hasło musi mieć co najmniej 6 znaków");
            return;
        }

        if (!password.equals(confirmPassword)) {
            NotificationUtils.showError("Hasła nie są identyczne");
            return;
        }

        // Sprawdzenie czy użytkownik już istnieje
        if (userService.findByEmail(email).isPresent()) {
            NotificationUtils.showError("Użytkownik o podanym emailu już istnieje");
            return;
        }

        // Mapowanie roli
        Role userRole = "Kierownik".equals(role) ? Role.MANAGER : Role.CLEANER;

        // Tworzenie użytkownika
        UserRequest request = new UserRequest(firstName, lastName, email, password, userRole);
        User user = userMapper.toEntity(request);
        userService.create(user);

        NotificationUtils.showSuccess("Rejestracja udana! Możesz się teraz zalogować.");
        
        // Automatyczne logowanie po rejestracji
        handleLogin(email, password);
    }
}
