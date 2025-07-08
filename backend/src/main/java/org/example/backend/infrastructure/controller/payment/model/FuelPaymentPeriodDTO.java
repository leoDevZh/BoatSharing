package org.example.backend.infrastructure.controller.payment.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuelPaymentPeriodDTO {
    @NotNull
    LocalDateTime startDate;

    @NotNull
    LocalDateTime endDate;
}
