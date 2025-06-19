package org.example.backend.infrastructure.repository.reservation;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.ReservationId;

public class ReservationMapper {
    static org.example.backend.infrastructure.repository.reservation.Reservation toEntity(org.example.backend.domain.reservation.model.Reservation reservation) {
        Long id = reservation.getReservationId() == null ? null : reservation.getReservationId().value();
        return org.example.backend.infrastructure.repository.reservation.Reservation.builder()
                .id(id)
                .startDateTime(reservation.getStartDateTime())
                .endDateTime(reservation.getEndDateTime())
                .boatHoursOnStart(reservation.getBoatHoursOnStart())
                .boatHoursOnEnd(reservation.getBoatHoursOnEnd())
                .boatId(reservation.getBoatId().value())
                .userId(reservation.getUserId().value())
                .build();
    }

    static org.example.backend.domain.reservation.model.Reservation toDomain(org.example.backend.infrastructure.repository.reservation.Reservation reservation) {
        return org.example.backend.domain.reservation.model.Reservation.builder()
                .reservationId(new ReservationId(reservation.getId()))
                .startDateTime(reservation.getStartDateTime())
                .endDateTime(reservation.getEndDateTime())
                .boatHoursOnStart(reservation.getBoatHoursOnStart())
                .boatHoursOnEnd(reservation.getBoatHoursOnEnd())
                .boatId(new BoatId(reservation.getBoatId()))
                .userId(new UserId(reservation.getUserId()))
                .build();
    }
}
