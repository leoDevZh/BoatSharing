package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.payment.model.DebtStatus;

import java.time.LocalDateTime;

public record DebtPaymentDTO(
        Long paymentId,
        LocalDateTime payedAt,
        String reason,
        Long creditorId,
        String creditorUsername,
        Long debtId,
        Double amount,
        DebtStatus debtStatus,
        Long debitorUserId,
        String debitorUsername
) {
}
