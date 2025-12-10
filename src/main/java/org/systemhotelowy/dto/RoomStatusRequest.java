package org.systemhotelowy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.RoomStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusRequest {
    @NotNull(message = "Status pokoju jest wymagany")
    private RoomStatus roomStatus;
}
