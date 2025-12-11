package org.systemhotelowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.systemhotelowy.model.Room;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByNumber(String number);

    long count();
}
