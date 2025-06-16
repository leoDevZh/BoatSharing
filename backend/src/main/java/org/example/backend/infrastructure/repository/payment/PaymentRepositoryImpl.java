package org.example.backend.infrastructure.repository.payment;

import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.spi.PaymentRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    public PaymentRepositoryImpl(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public PaymentId savePayment(Payment payment) {
        org.example.backend.infrastructure.repository.payment.Payment toSave = PaymentMapper.toEntity(payment);
        Long createdPayment = jpaPaymentRepository.save(toSave).getId();
        return new PaymentId(createdPayment);
    }
}
