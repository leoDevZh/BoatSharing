package org.example.backend.domain.reservation.model;

import org.example.backend.domain.User.UserDTO;
import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

public record ReservationUserDTO(
        ReservationId reservationId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer boatHoursOnStart,
        Integer boatHoursOnEnd,
        BoatId boatId,
        UserDTO userDTO
) {
}
