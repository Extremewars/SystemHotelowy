package org.systemhotelowy.mapper;

import org.springframework.stereotype.Component;
import org.systemhotelowy.dto.RoomRequest;
import org.systemhotelowy.dto.RoomResponse;
import org.systemhotelowy.model.Room;

@Component
public class RoomMapper {

    public Room toEntity(RoomRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RoomRequest nie może być nullem");
        }

        Room room = new Room();
        room.setNumber(request.getNumber());
        room.setFloor(request.getFloor());
        room.setType(request.getType());
        room.setRoomStatus(request.getRoomStatus());
        room.setCapacity(request.getCapacity());

        return room;
    }

    public RoomResponse toResponse(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("RoomResponse nie może być nullem");
        }

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setNumber(room.getNumber());
        response.setFloor(room.getFloor());
        response.setType(room.getType());
        response.setRoomStatus(room.getRoomStatus());
        response.setCapacity(room.getCapacity());

        return response;
    }
}
