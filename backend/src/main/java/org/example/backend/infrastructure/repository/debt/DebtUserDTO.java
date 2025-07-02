package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.payment.model.DebtStatus;

public record DebtUserDTO(
        Long debtId,
        Double amount,
        DebtStatus debtStatus,
        Long userId,
        String username
) {
}
