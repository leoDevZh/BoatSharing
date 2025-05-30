package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.Reservation;
import org.example.backend.domain.reservation.model.ReservationId;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CancelReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldDeleteReservationSuccessfully() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(userId, reservationId);

        verify(reservationRepository).deleteReservation(reservationId);
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidReservationException.class, () -> reservationService.cancelReservation(userId, reservationId));

        assertEquals("Reservation not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReservationInPast() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.of(2024, 4, 14, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 4, 14, 11, 0))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> reservationService.cancelReservation(userId, reservationId));

        assertEquals("Reservation is already in past", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserNotOwner() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        UserId reservationUserId = new UserId(2L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(reservationUserId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> reservationService.cancelReservation(userId, reservationId));

        assertEquals("User is not owner of reservation", exception.getMessage());
    }
}
