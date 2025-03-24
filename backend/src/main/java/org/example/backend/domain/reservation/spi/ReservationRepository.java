package org.example.backend.domain.reservation.spi;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.Reservation;

import java.time.LocalDateTime;

public interface ReservationRepository {
    Integer countOverlappingReservations(LocalDateTime from, LocalDateTime to, BoatId boatId);

    void saveNewReservation(Reservation reservation);
}
