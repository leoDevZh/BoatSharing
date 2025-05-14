package org.example.backend.infrastructure.repository.reservation;

import org.example.backend.domain.User.UserDTO;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.ReservationId;
import org.example.backend.domain.reservation.model.ReservationUserDTO;

public class ReservationUserMapper {
    public static ReservationUserDTO toDomain(org.example.backend.infrastructure.repository.reservation.ReservationUserDTO reservationUserDTO) {
        return new ReservationUserDTO(
                new ReservationId(reservationUserDTO.reservationId()),
                reservationUserDTO.startDateTime().withNano(0),
                reservationUserDTO.endDateTime().withNano(0),
                reservationUserDTO.boatHoursOnStart(),
                reservationUserDTO.boatHoursOnEnd(),
                new BoatId(reservationUserDTO.boatId()),
                new UserDTO(
                        new UserId(reservationUserDTO.userId()),
                        reservationUserDTO.username()
                )
        );
    }
}
