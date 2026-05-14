package org.example.backend.infrastructure.repository.invoice;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(name = "boat_id", nullable = false)
    private Long boatId;

    @Column(name = "total_hours", nullable = false)
    private Double totalHours;

    @Column(name = "total_payed", nullable = false)
    private Double totalPayed;
}
