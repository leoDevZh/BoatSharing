package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.Reservation;
import org.example.backend.domain.reservation.model.ReservationId;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UpdateReservationForReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2",
            "3, null",
            "null, 4"
    })
    void shouldUpdateReservationSuccessfully(String startStr, String endStr) {
        Integer boatHoursOnStart = "null".equals(startStr) ? null : Integer.valueOf(startStr);
        Integer boatHoursOnEnd = "null".equals(endStr) ? null : Integer.valueOf(endStr);
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(10))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.updateReservation(userId, reservationId, reservation.getStartDateTime(), reservation.getEndDateTime(), boatHoursOnStart, boatHoursOnEnd);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).findReservationById(reservationId);
        verify(reservationRepository, times(1)).saveReservation(reservationCaptor.capture());
        Reservation capturedReservation = reservationCaptor.getValue();
        assertEquals(reservation.getStartDateTime(), capturedReservation.getStartDateTime());
        assertEquals(reservation.getEndDateTime(), capturedReservation.getEndDateTime());
        assertEquals(boatHoursOnStart, capturedReservation.getBoatHoursOnStart());
        assertEquals(boatHoursOnEnd, capturedReservation.getBoatHoursOnEnd());
        assertEquals(reservationId, capturedReservation.getReservationId());
        assertEquals(userId, capturedReservation.getUserId());
    }

    @Test
    void shouldUpdateReservationBoatHoursSuccessfullyWhenInPast() {
        Integer boatHoursOnStart = 1;
        Integer boatHoursOnEnd = 2;
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now().minusHours(20))
                .endDateTime(LocalDateTime.now().minusHours(10))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.updateReservation(userId, reservationId, reservation.getStartDateTime(), reservation.getEndDateTime(), boatHoursOnStart, boatHoursOnEnd);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).findReservationById(reservationId);
        verify(reservationRepository, times(1)).saveReservation(reservationCaptor.capture());
        Reservation capturedReservation = reservationCaptor.getValue();
        assertEquals(reservation.getStartDateTime(), capturedReservation.getStartDateTime());
        assertEquals(reservation.getEndDateTime(), capturedReservation.getEndDateTime());
        assertEquals(boatHoursOnStart, capturedReservation.getBoatHoursOnStart());
        assertEquals(boatHoursOnEnd, capturedReservation.getBoatHoursOnEnd());
        assertEquals(reservationId, capturedReservation.getReservationId());
        assertEquals(userId, capturedReservation.getUserId());
    }

    @ParameterizedTest
    @MethodSource("provideReservationData")
    void shouldThrowExceptionWhenReservationInPastOrDateInvalid(LocalDateTime startDate, LocalDateTime endDate, String exceptionMsg) {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now().plusHours(10))
                .endDateTime(LocalDateTime.now().plusHours(20))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, startDate, endDate, 1, 2);
        });

        assertEquals(exceptionMsg, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReservationInPastAndTryToChangeDate() {
        LocalDateTime newStart = LocalDateTime.now().plusHours(10);
        LocalDateTime newEnd = LocalDateTime.now().plusHours(20);
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now().minusHours(20))
                .endDateTime(LocalDateTime.now().minusHours(10))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, newStart, newEnd, 1, 2);
        });

        assertEquals("Reservation update not possible in past", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "2050-03-23T10:00, null",
            "null, 2050-03-23T10:00",
    })
    void shouldThrowExceptionWhenDateIsNull(String startStr, String endStr) {
        LocalDateTime startDateTime = startStr.equals("null") ? null : LocalDateTime.parse(startStr);
        LocalDateTime endDateTime = endStr.equals("null") ? null : LocalDateTime.parse(endStr);
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        int boatHoursOnStar = 2;
        int boatHoursOnEnd = 1;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(boatHoursOnEnd))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, startDateTime, endDateTime, boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("Invalid reservation required values must not be null", exception.getMessage());
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
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(boatHoursOnEnd))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, reservation.getStartDateTime(), reservation.getEndDateTime(), boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("User is not owner of reservation", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOverlappingReservationsExist() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        int boatHoursOnStar = 1;
        int boatHoursOnEnd = 2;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(boatHoursOnEnd))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.countOverlappingReservationsForUpdate(reservation.getStartDateTime(), reservation.getEndDateTime(), reservation.getBoatId(), reservationId)).thenReturn(1);

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, reservation.getStartDateTime(), reservation.getEndDateTime(), boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("Reservation overlap with an existing reservation", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenStartEngineHourGreaterEndEngineHours() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        int boatHoursOnStar = 2;
        int boatHoursOnEnd = 1;
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .userId(userId)
                .boatId(new BoatId(1L))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(boatHoursOnEnd))
                .build();
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, reservation.getStartDateTime(), reservation.getEndDateTime(), boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("Start hours is greater then end hours", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        ReservationId reservationId = new ReservationId(1L);
        UserId userId = new UserId(1L);
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(10);
        int boatHoursOnStar = 1;
        int boatHoursOnEnd = 2;
        when(reservationRepository.findReservationById(reservationId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidReservationException.class, () -> {
            reservationService.updateReservation(userId, reservationId, start, end, boatHoursOnStar, boatHoursOnEnd);
        });

        assertEquals("Reservation not found", exception.getMessage());
    }

    private static Stream<Arguments> provideReservationData() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of(
                        now.plusHours(10),
                        now.plusHours(10),
                        "Start time is after end time"
                ),
                Arguments.of(
                        now.plusHours(15),
                        now.plusHours(10),
                        "Start time is after end time"
                ),
                Arguments.of(
                        now.minusHours(15),
                        now.minusHours(10),
                        "Reservation update not possible in past"
                )
        );
    }
}
