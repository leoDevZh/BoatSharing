package org.example.backend.infrastructure.repository.reservation;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
import org.example.backend.domain.reservation.spi.ReadReservationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReadReservationRepositoryImpl implements ReadReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReadReservationRepositoryImpl(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public List<ReservationUserDTO> findReservationsForPeriod(LocalDateTime from, LocalDateTime to, BoatId boatId) {
        List<org.example.backend.infrastructure.repository.reservation.ReservationUserDTO> reservations = jpaReservationRepository.findReservationsForPeriod(from, to, boatId.value());
        return reservations.stream().map(ReservationUserMapper::toDomain).toList();
    }
}
