package org.example.backend.domain.reservation.model;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

public record ReservationUserDTO(
        ReservationId reservationId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Double boatHoursOnStart,
        Double boatHoursOnEnd,
        BoatId boatId,
        UserDTO userDTO
) {
}
