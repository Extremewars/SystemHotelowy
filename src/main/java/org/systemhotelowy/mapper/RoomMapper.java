package org.systemhotelowy.mapper;

import org.springframework.stereotype.Component;
import org.systemhotelowy.dto.RoomRequest;
import org.systemhotelowy.dto.RoomResponse;
import org.systemhotelowy.model.Room;

@Component
public class RoomMapper {

    public Room toEntity(RoomRequest request) {
        if (request == null) {
            return null;
        }

        Room room = new Room();
        room.setNumber(request.getNumber());
        room.setFloor(request.getFloor());
        room.setType(request.getType());
        room.setRoomStatus(request.getRoomStatus());

        return room;
    }

    public RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setNumber(room.getNumber());
        response.setFloor(room.getFloor());
        response.setType(room.getType());
        response.setRoomStatus(room.getRoomStatus());

        return response;
    }
}
