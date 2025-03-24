package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    @Mock
    private BoatRepository boatRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        BoatId boatId = new BoatId(1L);
        UserId userId = new UserId(2L);
        Boat boat = mock(Boat.class);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.of(boat));
        when(boat.isCoOwner(userId)).thenReturn(true);
        when(reservationRepository.countOverlappingReservations(start, end, boatId)).thenReturn(0);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        assertDoesNotThrow(() -> reservationService.makeNewReservation(userId, boatId, start, end));
        verify(reservationRepository, times(1)).saveNewReservation(reservationCaptor.capture());
        Reservation reservationCaptured = reservationCaptor.getValue();
        assertEquals(start, reservationCaptured.startDateTime);
        assertEquals(end, reservationCaptured.endDateTime);
        assertEquals(boatId, reservationCaptured.boatId);
        assertEquals(userId, reservationCaptured.userId);
    }

    @Test
    void shouldThrowExceptionWhenBoatNotFound() {
        BoatId boatId = new BoatId(1L);
        UserId userId = new UserId(2L);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidReservationException.class, () ->
                reservationService.makeNewReservation(userId, boatId, start, end));

        assertEquals("Boat not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserNotCoOwner() {
        BoatId boatId = new BoatId(1L);
        Boat boat = mock(Boat.class);
        UserId userId = new UserId(2L);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.of(boat));
        when(boat.isCoOwner(userId)).thenReturn(false);

        Exception exception = assertThrows(InvalidReservationException.class, () ->
                reservationService.makeNewReservation(userId, boatId, start, end));

        assertEquals("User is not co-owner of boat", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOverlappingReservationsExist() {
        BoatId boatId = new BoatId(1L);
        Boat boat = mock(Boat.class);
        UserId userId = new UserId(2L);
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 1, 30);
        when(boatRepository.findByIdWithOwners(boatId)).thenReturn(Optional.of(boat));
        when(boat.isCoOwner(userId)).thenReturn(true);
        when(reservationRepository.countOverlappingReservations(start, end, boatId)).thenReturn(1);

        Exception exception = assertThrows(InvalidReservationException.class, () ->
                reservationService.makeNewReservation(userId, boatId, start, end));

        assertEquals("Reservation overlap with an existing reservation", exception.getMessage());
    }
}
