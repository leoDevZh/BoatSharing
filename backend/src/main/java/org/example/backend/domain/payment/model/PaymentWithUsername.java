package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.model.UserDTO;

import java.time.LocalDateTime;

public record PaymentWithUsername(
        PaymentId paymentId,
        LocalDateTime payedAt,
        Double amount,
        String reason,
        Boolean isFuelPayment,
        PaymentStatus paymentStatus,
        UserDTO creditor
) {
}
