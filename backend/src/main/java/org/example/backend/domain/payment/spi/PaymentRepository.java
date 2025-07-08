package org.example.backend.domain.payment.spi;

import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;

public interface PaymentRepository {

    PaymentId savePayment(Payment payment);

    void deletePayment(PaymentId paymentId);

    void setPaymentToClosed(PaymentId paymentId);
}
