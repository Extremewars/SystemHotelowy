package org.systemhotelowy.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "ID pokoju jest wymagane")
    private Integer roomId;

    @NotNull(message = "Data zameldowania jest wymagana")
    private LocalDate checkInDate;

    @NotNull(message = "Data wymeldowania jest wymagana")
    private LocalDate checkOutDate;

    @NotBlank(message = "Imię i nazwisko gościa jest wymagane")
    private String guestName;

    @NotBlank(message = "Email gościa jest wymagany")
    @Email(message = "Email gościa musi być poprawny")
    private String guestEmail;

    @NotBlank(message = "Telefon gościa jest wymagany")
    private String guestPhone;

    @NotNull(message = "Liczba gości jest wymagana")
    @Min(value = 1, message = "Liczba gości musi być co najmniej 1")
    private Integer numberOfGuests;

    @NotNull(message = "Cena całkowita jest wymagana")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cena musi być większa od 0")
    private BigDecimal totalPrice;


    private ReservationStatus status;

    private String notes;
}
