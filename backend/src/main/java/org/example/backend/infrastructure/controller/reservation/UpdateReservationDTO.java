package org.example.backend.infrastructure.controller.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.backend.domain.reservation.model.ReservationId;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UpdateReservationDTO {
    @NotNull
    LocalDateTime startDateTime;

    @NotNull
    LocalDateTime endDateTime;

    Integer boatEngineHoursOnStart;

    Integer boatEngineHoursOnEnd;

    @NotNull
    ReservationId reservationId;
}
