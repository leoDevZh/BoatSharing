package org.example.backend.infrastructure.repository.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.model.Reservation;
import org.example.backend.domain.reservation.model.ReservationId;
import org.example.backend.domain.reservation.spi.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public Integer countOverlappingReservationsForUpdate(LocalDateTime from, LocalDateTime to, BoatId boatId, ReservationId reservationId) {
        return jpaReservationRepository.countOverlappingReservationsForUpdate(from, to, boatId.value(), reservationId.value());
    }

    @Override
    public void saveReservation(Reservation reservation) {
        jpaReservationRepository.save(ReservationMapper.toEntity(reservation));
    }

    @Override
    public Optional<Reservation> findReservationById(ReservationId reservationId) {
        Optional<org.example.backend.infrastructure.repository.reservation.Reservation> reservationOptional = jpaReservationRepository.findById(reservationId.value());
        return reservationOptional.map(ReservationMapper::toDomain);
    }

    @Override
    public void deleteReservation(ReservationId reservationId) {
        jpaReservationRepository.deleteById(reservationId.value());
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

        static Reservation toDomain(org.example.backend.infrastructure.repository.reservation.Reservation reservation) {
            return Reservation.builder()
                    .reservationId(new ReservationId(reservation.getId()))
                    .startDateTime(reservation.getStartDateTime())
                    .endDateTime(reservation.getEndDateTime())
                    .boatHoursOnStart(reservation.getBoatHoursOnStart())
                    .boatHoursOnEnd(reservation.getBoatHoursOnEnd())
                    .boatId(new BoatId(reservation.getBoatId()))
                    .userId(new UserId(reservation.getUserId()))
                    .build();
        }
    }
}
