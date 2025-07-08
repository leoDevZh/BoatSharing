package org.example.backend.infrastructure.repository.reservation;

import java.time.LocalDateTime;

public record ReservationUserDTO(
        Long reservationId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Double boatHoursOnStart,
        Double boatHoursOnEnd,
        Long boatId,
        Long userId,
        String username
) {
}
