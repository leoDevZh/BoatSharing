package org.example.backend.infrastructure.repository.reservation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "boat_hours_on_start")
    private Double boatHoursOnStart;

    @Column(name = "boat_hours_on_end")
    private Double boatHoursOnEnd;

    @Column(name = "boat_id", nullable = false)
    private Long boatId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
