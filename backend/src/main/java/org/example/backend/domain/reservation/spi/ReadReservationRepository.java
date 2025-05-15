package org.example.backend.domain.reservation.spi;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.ReservationUserDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadReservationRepository {
    List<ReservationUserDTO> findReservationsForPeriod(LocalDateTime from, LocalDateTime to, BoatId boatId);
}
