package org.systemhotelowy.unit.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.mapper.ReservationMapper;
import org.systemhotelowy.model.ReservationStatus;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.RoomType;
import org.systemhotelowy.repository.ReservationRepository;
import org.systemhotelowy.repository.RoomRepository;
import org.systemhotelowy.service.impl.ReservationServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationMapper reservationMapper;

    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                reservationRepository,
                roomRepository,
                reservationMapper
        );
    }

    // -------------------------------------------------
    // Walidacja dat
    // -------------------------------------------------
    @Test
    void create_shouldThrowWhenCheckOutBeforeCheckIn() {
        // given
        LocalDate checkIn = LocalDate.of(2025, 12, 10);
        LocalDate checkOut = LocalDate.of(2025, 12, 9); // wcześniejsza data

        ReservationRequest request = new ReservationRequest(
                1,                          // roomId
                checkIn,
                checkOut,
                "Test Guest",
                "guest@example.com",
                "123456789",
                1,                          // numberOfGuests
                BigDecimal.TEN,
                ReservationStatus.PENDING,
                "test notes"
        );

        // when + then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data wymeldowania musi być późniejsza");
    }

    // -------------------------------------------------
    //  Walidacja liczby gości
    // -------------------------------------------------
    @Test
    void create_shouldThrowWhenGuestCountExceedsRoomCapacity() {
        // given
        LocalDate checkIn = LocalDate.of(2025, 12, 9);
        LocalDate checkOut = LocalDate.of(2025, 12, 10);

        ReservationRequest request = new ReservationRequest(
                1,                          // roomId
                checkIn,
                checkOut,
                "Test Guest",
                "guest@example.com",
                "123456789",
                3,                          // numberOfGuests (za dużo)
                BigDecimal.TEN,
                ReservationStatus.PENDING,
                "test notes"
        );

        // pokój o pojemności 1
        Room room = new Room();
        room.setId(1);
        room.setNumber("101");
        room.setFloor(1);
        room.setCapacity(1);
        room.setType(RoomType.SINGLE);
        room.setRoomStatus(RoomStatus.READY);

        when(roomRepository.findById(1))
                .thenReturn(java.util.Optional.of(room));

        // when + then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
