package org.example.backend.infrastructure.controller.converter;

import org.example.backend.domain.reservation.ReservationId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToReservationIdConverter implements Converter<String, ReservationId> {
    @Override
    public ReservationId convert(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Received reservationId is null or empty");
        }
        try {
            return new ReservationId(Long.parseLong(source));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Received reservationId is in the wrong format");
        }
    }
}
