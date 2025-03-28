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
        Boat boat = boatRepository.findByIdWithOwners(boatId).orElseThrow(() -> new InvalidReservationException("Boat not found"));
        if (!boat.isCoOwner(userId)) {
            throw new InvalidReservationException("User is not co-owner of boat");
        }
        if (reservationRepository.countOverlappingReservations(start, end, boatId) > 0) {
            throw new InvalidReservationException("Reservation overlap with an existing reservation");
        }
        Reservation newReservation = Reservation.builder()
                .userId(userId)
                .boatId(boatId)
                .startDateTime(start)
                .endDateTime(end)
                .build();
        reservationRepository.saveReservation(newReservation);
    }

    @Override
    public void updateEngineHoursForReservation(UserId userId, ReservationId reservationId, int boatHoursOnStar, int boatHoursOnEnd) {
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new InvalidReservationException("Reservation not found"));
        if (!reservation.isOwner(userId)) {
            throw new InvalidReservationException("User is not owner of reservation");
        }
        reservation.updateBoatEngineHours(boatHoursOnStar, boatHoursOnEnd);
        reservationRepository.saveReservation(reservation);
    }
}
