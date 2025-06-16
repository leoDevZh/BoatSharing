package org.example.backend.infrastructure.repository.payment;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;

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
}
