package org.example.backend.infrastructure.controller.converter;

import org.example.backend.domain.boat.BoatId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToBoatIdConverter implements Converter<String, BoatId> {
    @Override
    public BoatId convert(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Received boatId is null or empty");
        }
        try {
            return new BoatId(Long.parseLong(source));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Received boatId is in the wrong format");
        }
    }
}
