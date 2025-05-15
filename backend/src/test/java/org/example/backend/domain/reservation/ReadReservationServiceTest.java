package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserDTO;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.example.backend.domain.reservation.model.ReservationId;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
import org.example.backend.domain.reservation.spi.ReadReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadReservationServiceTest {

    @Mock
    private BoatRepository boatRepository;

    @Mock
    private ReadReservationRepository readReservationRepository;

    @InjectMocks
    private ReadReservationServiceImpl readReservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReadReservationSuccessfully() {
        BoatId boatId = new BoatId(1L);
        UserId userId = new UserId(2L);
        Boat boat = mock(Boat.class);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        List<ReservationUserDTO> expectedReservations = List.of(
                new ReservationUserDTO(new ReservationId(1L), start, end, 10, 20, boatId, new UserDTO(userId, "username"))
        );
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.of(boat));
        when(boat.isCoOwner(userId)).thenReturn(true);
        when(readReservationRepository.findReservationsForPeriod(start, end, boatId)).thenReturn(expectedReservations);

        List<ReservationUserDTO> reservations = readReservationService.getReservationForPeriod(start, end, boatId, userId);

        assertTrue(expectedReservations.containsAll(reservations) && expectedReservations.size() == reservations.size());
    }

    @Test
    void shouldThrowExceptionWhenBoatNotFound() {
        BoatId boatId = new BoatId(1L);
        UserId userId = new UserId(2L);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidReservationException.class, () -> readReservationService.getReservationForPeriod(start, end, boatId, userId));

        assertEquals("Boat not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwner() {
        BoatId boatId = new BoatId(1L);
        UserId userId = new UserId(2L);
        Boat boat = mock(Boat.class);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.of(boat));
        when(boat.isCoOwner(userId)).thenReturn(false);

        Exception exception = assertThrows(InvalidReservationException.class, () -> readReservationService.getReservationForPeriod(start, end, boatId, userId));

        assertEquals("User is not co-owner of boat", exception.getMessage());
    }
}
