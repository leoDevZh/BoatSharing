package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

public class Reservation {
    ReservationId reservationId;
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    Integer boatHoursOnStart;
    Integer boatHoursOnEnd;
    BoatId boatId;
    UserId userId;

    public Reservation(LocalDateTime startDateTime, LocalDateTime endDateTime, BoatId boatId, UserId userId) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.boatId = boatId;
        this.userId = userId;
    }

    public ReservationId getReservationId() {
        return reservationId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public Integer getBoatHoursOnEnd() {
        return boatHoursOnEnd;
    }

    public UserId getUserId() {
        return userId;
    }

    public BoatId getBoatId() {
        return boatId;
    }

    public Integer getBoatHoursOnStart() {
        return boatHoursOnStart;
    }
}
