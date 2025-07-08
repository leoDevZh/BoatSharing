package org.example.backend.domain.payment.model;

import java.time.LocalDateTime;

public record FuelPaymentPeriodDTO(LocalDateTime startDate, LocalDateTime endDate) {
}
