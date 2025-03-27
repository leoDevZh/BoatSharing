package org.example.backend.domain.reservation.api;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.InvalidReservationException;
import org.example.backend.domain.reservation.ReservationId;

import java.time.LocalDateTime;

public interface ReservationService {
    void makeNewReservation(UserId userId, BoatId boatId, LocalDateTime start, LocalDateTime end) throws InvalidReservationException;

    void updateEngineHoursForReservation(UserId userId, ReservationId reservationId, int boatHoursOnStar, int boatHoursOnEnd);
}
