package org.example.backend.infrastructure.controller.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateReservationDTO {
    @NotNull
    LocalDateTime startTime;
    @NotNull
    LocalDateTime endTime;
    @NotNull
    BoatId boatId;
}
