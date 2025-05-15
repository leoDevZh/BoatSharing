package org.example.backend.domain.reservation.api;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.ReservationUserDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadReservationService {
    List<ReservationUserDTO> getReservationForPeriod(LocalDateTime from, LocalDateTime to, BoatId boatId, UserId userId);
}
