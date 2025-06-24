package org.example.backend.infrastructure.controller.converter;

import org.example.backend.domain.payment.model.DebtId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDebtIdConverter implements Converter<String, DebtId> {
    @Override
    public DebtId convert(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Received debtId is null or empty");
        }
        try {
            return new DebtId(Long.parseLong(source));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Received debtId is in the wrong format");
        }
    }
}
