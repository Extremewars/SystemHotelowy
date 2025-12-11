package org.systemhotelowy.service;

import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room create(Room room);

    Optional<Room> findById(Integer id);

    List<Room> findAll();

    Optional<Room> findByNumber(String number);

    Room update(Room room);

    void deleteById(Integer id);

    Room updateStatus(Integer id, RoomStatus status);

    long countRooms();
}
