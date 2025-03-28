package org.example.backend.domain.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

public class Reservation {
    private ReservationId reservationId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer boatHoursOnStart;
    private Integer boatHoursOnEnd;
    private BoatId boatId;
    private UserId userId;

    private Reservation(ReservationBuilder reservationBuilder) {
        if (reservationBuilder.userId == null || reservationBuilder.boatId == null || reservationBuilder.startDateTime == null || reservationBuilder.endDateTime == null) {
            throw new InvalidReservationException("Invalid reservation required values must not be null");
        }
        if (!reservationBuilder.startDateTime.isBefore(reservationBuilder.endDateTime)) {
            throw new InvalidReservationException("Start time is after end time");
        }
        checkBoatHoursValid(reservationBuilder.boatHoursOnStart, reservationBuilder.boatHoursOnEnd);
        this.reservationId = reservationBuilder.reservationId;
        this.startDateTime = reservationBuilder.startDateTime;
        this.endDateTime = reservationBuilder.endDateTime;
        this.boatHoursOnStart = reservationBuilder.boatHoursOnStart;
        this.boatHoursOnEnd = reservationBuilder.boatHoursOnEnd;
        this.boatId = reservationBuilder.boatId;
        this.userId = reservationBuilder.userId;
    }

    public void updateBoatEngineHours(int updatedBoatHoursOnStar, int updatedBoatHoursOnEnd) {
        checkBoatHoursValid(updatedBoatHoursOnStar, updatedBoatHoursOnEnd);
        this.boatHoursOnStart = updatedBoatHoursOnStar;
        this.boatHoursOnEnd = updatedBoatHoursOnEnd;
    }

    private static void checkBoatHoursValid(Integer updatedBoatHoursOnStar, Integer updatedBoatHoursOnEnd) {
        if (updatedBoatHoursOnStar == null || updatedBoatHoursOnEnd == null) {
            return;
        }
        if (updatedBoatHoursOnStar > updatedBoatHoursOnEnd) {
            throw new InvalidReservationException("Start hours is greater then end hours");
        }
    }

    public boolean isOwner(UserId userId) {
        return this.userId.equals(userId);
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

    public static ReservationBuilder builder() {
        return new ReservationBuilder();
    }

    public static class ReservationBuilder {
        ReservationId reservationId;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        Integer boatHoursOnStart;
        Integer boatHoursOnEnd;
        BoatId boatId;
        UserId userId;

        private ReservationBuilder() {
        }

        public ReservationBuilder reservationId(ReservationId reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public ReservationBuilder startDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public ReservationBuilder endDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public ReservationBuilder boatHoursOnStart(Integer boatHoursOnStart) {
            this.boatHoursOnStart = boatHoursOnStart;
            return this;
        }

        public ReservationBuilder boatHoursOnEnd(Integer boatHoursOnEnd) {
            this.boatHoursOnEnd = boatHoursOnEnd;
            return this;
        }

        public ReservationBuilder boatId(BoatId boatId) {
            this.boatId = boatId;
            return this;
        }

        public ReservationBuilder userId(UserId userId) {
            this.userId = userId;
            return this;
        }

        public Reservation build() {
            return new Reservation(this);
        }
    }
}
