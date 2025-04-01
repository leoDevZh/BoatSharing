package org.example.backend.domain.reservation.spi;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.Reservation;
import org.example.backend.domain.reservation.ReservationId;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository {
    Integer countOverlappingReservations(LocalDateTime from, LocalDateTime to, BoatId boatId);

    void saveReservation(Reservation reservation);

    Optional<Reservation> findReservationById(ReservationId reservationId);

    void deleteReservation(ReservationId reservationId);
}
