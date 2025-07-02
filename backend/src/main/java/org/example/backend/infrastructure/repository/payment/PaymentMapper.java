package org.example.backend.infrastructure.repository.payment;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.model.PaymentWithUsername;

public class PaymentMapper {

    static org.example.backend.infrastructure.repository.payment.Payment toEntity(Payment payment) {
        Long id = payment.getPaymentId() == null ? null : payment.getPaymentId().value();
        return org.example.backend.infrastructure.repository.payment.Payment.builder()
                .id(id)
                .paymentDate(payment.getPayedAt())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .reason(payment.getReason())
                .isFuelPayment(payment.isFuelPayment())
                .userId(payment.getUserId().value())
                .build();
    }

    static Payment toDomain(org.example.backend.infrastructure.repository.payment.Payment payment) {
        return Payment.builder()
                .paymentId(new PaymentId(payment.getId()))
                .payedAt(payment.getPaymentDate())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .reason(payment.getReason())
                .fuelPayment(payment.getIsFuelPayment())
                .userId(new UserId(payment.getUserId()))
                .build();
    }

    static PaymentWithUsername toDomain(org.example.backend.infrastructure.repository.payment.model.PaymentWithUsername payment) {
        return new PaymentWithUsername(
                new PaymentId(payment.paymentId()),
                payment.payedAt(),
                payment.amount(),
                payment.reason(),
                payment.isFuelPayment(),
                payment.paymentStatus(),
                new UserDTO(
                        new UserId(payment.creditorId()),
                        payment.creditorUsername()
                )
        );
    }
}
