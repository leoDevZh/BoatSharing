package org.example.backend.infrastructure.controller.converter;

import org.example.backend.domain.payment.model.PaymentId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPaymentIdConverter implements Converter<String, PaymentId> {
    @Override
    public PaymentId convert(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Received paymentId is null or empty");
        }
        try {
            return new PaymentId(Long.parseLong(source));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Received paymentId is in the wrong format");
        }
    }
}
