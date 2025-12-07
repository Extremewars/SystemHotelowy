package org.systemhotelowy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.RoomType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Integer id;
    private String number;
    private Integer floor;
    private RoomType type;
    private RoomStatus roomStatus;
    private Integer capacity;
}
