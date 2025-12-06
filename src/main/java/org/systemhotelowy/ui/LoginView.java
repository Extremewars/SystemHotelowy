package org.systemhotelowy.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends VerticalLayout {

    public LoginView() {
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

        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setWidthFull();

        Button loginButton = new Button("Zaloguj");
        loginButton.setWidthFull();

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

        EmailField emailRegister = new EmailField("Email");
        emailRegister.setWidthFull();

        PasswordField regPassword = new PasswordField("Hasło");
        regPassword.setWidthFull();

        PasswordField regConfirmPassword = new PasswordField("Potwierdź hasło");
        regConfirmPassword.setWidthFull();

        RadioButtonGroup<String> roleSelector = new RadioButtonGroup<>();
        roleSelector.setLabel("Rola");
        roleSelector.setItems("Pracownik", "Kierownik");

        Button registerButton = new Button("Zarejestruj");
        registerButton.setWidthFull();

        Span switchToLogin = new Span("Masz już konto? Zaloguj się");
        switchToLogin.getStyle().set("cursor", "pointer");
        switchToLogin.getStyle().set("color", "blue");
        switchToLogin.getStyle().set("text-decoration", "underline");

        registerLayout.add(registerTitle, emailRegister, regPassword, regConfirmPassword, roleSelector, registerButton, switchToLogin);
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
}
