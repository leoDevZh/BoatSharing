package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.model.UserDTO;

import java.time.LocalDateTime;

public record DebtPaymentDTO(
        PaymentId paymentId,
        LocalDateTime payedAt,
        String reason,
        UserDTO creditor,
        DebtUserDTO debtUserDTO
) {
}
