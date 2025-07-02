package org.example.backend.infrastructure.repository.payment.model;

import org.example.backend.domain.payment.model.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentWithUsername(
        Long paymentId,
        LocalDateTime payedAt,
        Double amount,
        String reason,
        Boolean isFuelPayment,
        PaymentStatus paymentStatus,
        Long creditorId,
        String creditorUsername
) {
}
