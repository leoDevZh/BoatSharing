package org.example.backend.domain.reservation.api;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.InvalidReservationException;
import org.example.backend.domain.reservation.model.ReservationId;

import java.time.LocalDateTime;

public interface ReservationService {
    void makeNewReservation(UserId userId, BoatId boatId, LocalDateTime start, LocalDateTime end) throws InvalidReservationException;

    void updateReservation(UserId userId, ReservationId reservationId, LocalDateTime start, LocalDateTime end, Integer boatHoursOnStar, Integer boatHoursOnEnd);

    void cancelReservation(UserId userId, ReservationId reservationId);
}
