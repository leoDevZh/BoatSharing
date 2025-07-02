package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.model.UserDTO;

public record DebtUserDTO(
        DebtId debtId,
        Double amount,
        DebtStatus debtStatus,
        UserDTO debitor
) {
}
