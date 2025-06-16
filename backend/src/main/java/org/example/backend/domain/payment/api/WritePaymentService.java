package org.example.backend.domain.payment.api;

import org.example.backend.domain.payment.model.CreatePayment;

public interface WritePaymentService {
    void createPayment(CreatePayment createPayment);
}
