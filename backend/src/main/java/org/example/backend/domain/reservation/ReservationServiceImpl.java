package org.example.backend.domain.reservation;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.example.backend.domain.reservation.api.ReservationService;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@DomainService
public class ReservationServiceImpl implements ReservationService {

    private final BoatRepository boatRepository;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(BoatRepository boatRepository, ReservationRepository reservationRepository) {
        this.boatRepository = boatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void makeNewReservation(UserId userId, BoatId boatId, LocalDateTime start, LocalDateTime end) throws InvalidReservationException {
        if (!start.isBefore(end)) {
            throw new InvalidReservationException("Start time is after end time");
        }
        Boat boat = boatRepository.findByIdWithOwners(boatId).orElseThrow(() -> new InvalidReservationException("Boat not found"));
        if (!boat.isCoOwner(userId)) {
            throw new InvalidReservationException("User is not co-owner of boat");
        }
        if (reservationRepository.countOverlappingReservations(start, end, boatId) > 0) {
            throw new InvalidReservationException("Reservation overlap with an existing reservation");
        }
        Reservation newReservation = new Reservation(start, end, boatId, userId);
        reservationRepository.saveNewReservation(newReservation);
    }
}
