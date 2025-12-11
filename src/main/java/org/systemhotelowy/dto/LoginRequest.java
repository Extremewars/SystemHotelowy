package org.systemhotelowy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Email musi być poprawny")
    private String email;

    @NotBlank(message = "Hasło nie może być puste")
    private String password;
}