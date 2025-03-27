package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UpdateEngineHoursForReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpdateEngineHoursForReservationSuccessfully() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        int boatHoursOnStar = 1;
        int boatHoursOnEnd = 2;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.updateEngineHoursForReservation(userId, reservationId, boatHoursOnStar, boatHoursOnEnd);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).findReservationById(reservationId);
        verify(reservationRepository, times(1)).saveReservation(reservationCaptor.capture());
        Reservation capturedReservation = reservationCaptor.getValue();
        assertEquals(boatHoursOnStar, capturedReservation.getBoatHoursOnStart());
        assertEquals(boatHoursOnEnd, capturedReservation.getBoatHoursOnEnd());
        assertEquals(reservationId, capturedReservation.getReservationId());
        assertEquals(userId, capturedReservation.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotOwner() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        UserId reservationUserId = new UserId(2L);
        int boatHoursOnStar = 1;
        int boatHoursOnEnd = 2;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(reservationUserId)
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateEngineHoursForReservation(userId, reservationId, boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("User is not owner of reservation", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenStartGreaterEnd() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        int boatHoursOnStar = 2;
        int boatHoursOnEnd = 1;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateEngineHoursForReservation(userId, reservationId, boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("Start hours is greater then end hours", exception.getMessage());
    }
}
