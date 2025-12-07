package org.systemhotelowy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.RoomType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {

    @NotBlank(message = "Numer pokoju nie może być pusty")
    private String number;

    private Integer floor;

    @NotNull(message = "Typ pokoju jest wymagany")
    private RoomType type;

    @NotNull(message = "Status pokoju jest wymagany")
    private RoomStatus roomStatus;

    @NotNull(message = "Pojemność pokoju jest wymagana")
    private Integer capacity;
}
