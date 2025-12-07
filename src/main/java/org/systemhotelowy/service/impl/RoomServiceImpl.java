package org.systemhotelowy.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.repository.RoomRepository;
import org.systemhotelowy.service.RoomService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room create(Room room) {
        return roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findById(Integer id) {
        return roomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findByNumber(String number) {
        return roomRepository.findByNumber(number);
    }

    @Override
    public Room update(Room room) {
        Integer id = room.getId();
        if (id == null) {
            throw new IllegalArgumentException("Room id must not be null for update.");
        }
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Room with id " + id + " not found."));

        existing.setNumber(room.getNumber());
        existing.setFloor(room.getFloor());
        existing.setType(room.getType());
        existing.setRoomStatus(room.getRoomStatus());
        existing.setCapacity(room.getCapacity());

        return roomRepository.save(existing);
    }

    @Override
    public void deleteById(Integer id) {
        roomRepository.deleteById(id);
    }

    @Override
    public Room updateStatus(Integer id, RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Room with id " + id + " not found."));
        room.setRoomStatus(status);
        return roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRooms() {
        return roomRepository.count();
    }
}
