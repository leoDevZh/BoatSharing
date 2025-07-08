package org.example.backend.domain.reservation.model;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.InvalidReservationException;

import java.time.LocalDateTime;

public class Reservation {
    private ReservationId reservationId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double boatHoursOnStart;
    private Double boatHoursOnEnd;
    private BoatId boatId;
    private UserId userId;

    private Reservation(ReservationBuilder reservationBuilder) {
        if (reservationBuilder.userId == null || reservationBuilder.boatId == null || reservationBuilder.startDateTime == null || reservationBuilder.endDateTime == null) {
            throw new InvalidReservationException("Invalid reservation required values must not be null");
        }
        checkEndTimeIsAfterStartTime(reservationBuilder.startDateTime, reservationBuilder.endDateTime);
        checkBoatHoursValid(reservationBuilder.boatHoursOnStart, reservationBuilder.boatHoursOnEnd);
        this.reservationId = reservationBuilder.reservationId;
        this.startDateTime = reservationBuilder.startDateTime;
        this.endDateTime = reservationBuilder.endDateTime;
        this.boatHoursOnStart = reservationBuilder.boatHoursOnStart;
        this.boatHoursOnEnd = reservationBuilder.boatHoursOnEnd;
        this.boatId = reservationBuilder.boatId;
        this.userId = reservationBuilder.userId;
    }

    public void cancelReservation() {
        checkDateInPast(endDateTime);
    }

    public void updateReservation(LocalDateTime start, LocalDateTime end, Double updatedBoatHoursOnStar, Double updatedBoatHoursOnEnd) {
        checkEndTimeIsAfterStartTime(start, end);
        if (!(startDateTime.equals(start) && endDateTime.equals(end))) {
            checkDateInPast(endDateTime);
            checkDateInPast(end);
        }
        checkBoatHoursValid(updatedBoatHoursOnStar, updatedBoatHoursOnEnd);
        this.startDateTime = start;
        this.endDateTime = end;
        this.boatHoursOnStart = updatedBoatHoursOnStar;
        this.boatHoursOnEnd = updatedBoatHoursOnEnd;
    }

    private static void checkEndTimeIsAfterStartTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidReservationException("Invalid reservation required values must not be null");
        }
        if (!start.isBefore(end)) {
            throw new InvalidReservationException("Start time is after end time");
        }
    }

    private void checkDateInPast(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("Reservation update not possible in past");
        }
    }

    private static void checkBoatHoursValid(Double updatedBoatHoursOnStar, Double updatedBoatHoursOnEnd) {
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

    public Double getBoatHoursOnEnd() {
        return boatHoursOnEnd;
    }

    public UserId getUserId() {
        return userId;
    }

    public BoatId getBoatId() {
        return boatId;
    }

    public Double getBoatHoursOnStart() {
        return boatHoursOnStart;
    }

    public static ReservationBuilder builder() {
        return new ReservationBuilder();
    }

    public static class ReservationBuilder {
        ReservationId reservationId;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        Double boatHoursOnStart;
        Double boatHoursOnEnd;
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

        public ReservationBuilder boatHoursOnStart(Double boatHoursOnStart) {
            this.boatHoursOnStart = boatHoursOnStart;
            return this;
        }

        public ReservationBuilder boatHoursOnEnd(Double boatHoursOnEnd) {
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
