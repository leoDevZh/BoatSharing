package org.example.backend.infrastructure.repository.reservation;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.Reservation;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationRepositoryImplementation implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryImplementation(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Integer countOverlappingReservations(LocalDateTime from, LocalDateTime to, BoatId boatId) {
        return jpaReservationRepository.countOverlappingReservations(from, to, boatId.value());
    }

    @Override
    public void saveNewReservation(Reservation reservation) {
        jpaReservationRepository.save(ReservationMapper.toEntity(reservation));
    }

    static class ReservationMapper {

        static org.example.backend.infrastructure.repository.reservation.Reservation toEntity(Reservation reservation) {
            Long id = reservation.getReservationId() == null ? null : reservation.getReservationId().value();
            return org.example.backend.infrastructure.repository.reservation.Reservation.builder()
                    .id(id)
                    .startDateTime(reservation.getStartDateTime())
                    .endDateTime(reservation.getEndDateTime())
                    .boatHoursOnStart(reservation.getBoatHoursOnStart())
                    .boatHoursOnEnd(reservation.getBoatHoursOnEnd())
                    .boatId(reservation.getBoatId().value())
                    .userId(reservation.getUserId().value())
                    .build();
        }
    }
}
