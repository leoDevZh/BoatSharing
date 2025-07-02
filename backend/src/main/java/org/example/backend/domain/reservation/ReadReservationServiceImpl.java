package org.example.backend.domain.reservation;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.example.backend.domain.reservation.api.ReadReservationService;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
import org.example.backend.domain.reservation.spi.ReadReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@DomainService
public class ReadReservationServiceImpl implements ReadReservationService {
    private final BoatRepository boatRepository;
    private final ReadReservationRepository readReservationRepository;

    public ReadReservationServiceImpl(BoatRepository boatRepository, ReadReservationRepository readReservationRepository) {
        this.boatRepository = boatRepository;
        this.readReservationRepository = readReservationRepository;
    }

    @Override
    public List<ReservationUserDTO> getReservationForPeriod(LocalDateTime from, LocalDateTime to, BoatId boatId, UserId userId) throws InvalidReservationException {
        Boat boat = boatRepository.findByIdWithOwners(boatId).orElseThrow(() -> new InvalidReservationException("Boat not found"));
        if (!boat.isCoOwner(userId)) {
            throw new InvalidReservationException("User is not co-owner of boat");
        }

        return readReservationRepository.findReservationsForPeriod(from, to, boatId);
    }
}
