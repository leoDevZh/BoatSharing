package org.example.backend.infrastructure.repository.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.startDateTime < :to AND r.endDateTime > :from AND r.boatId = :boatId")
    Integer countOverlappingReservations(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("boatId") Long boatId);

    @Query("""
                SELECT new org.example.backend.infrastructure.repository.reservation.ReservationUserDTO(
                    r.id, r.startDateTime, r.endDateTime, r.boatHoursOnStart, r.boatHoursOnEnd, r.boatId,
                    u.id, u.username
                )
                FROM Reservation r
                JOIN User u ON u.id = r.userId
                WHERE r.startDateTime >= :from
                  AND r.startDateTime < :to
                  AND r.boatId = :boatId
            """)
    List<ReservationUserDTO> findReservationsForPeriod(@Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to,
                                                       @Param("boatId") Long boatId);
}
