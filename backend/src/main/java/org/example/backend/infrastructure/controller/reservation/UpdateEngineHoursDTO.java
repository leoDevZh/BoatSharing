package org.example.backend.infrastructure.controller.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.domain.reservation.model.ReservationId;

@Data
@AllArgsConstructor
public class UpdateEngineHoursDTO {
    @NotNull
    Integer boatEngineHoursOnStart;
    @NotNull
    Integer boatEngineHoursOnEnd;
    @NotNull
    ReservationId reservationId;
}
