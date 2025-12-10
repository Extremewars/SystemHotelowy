package org.systemhotelowy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dla żądań dotyczących rezerwacji (tworzenie/edycja).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    private Integer roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String notes;
}
