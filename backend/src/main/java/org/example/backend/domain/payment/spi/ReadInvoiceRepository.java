package org.example.backend.domain.payment.spi;

import org.example.backend.domain.boat.BoatId;

import java.time.LocalDateTime;

public interface ReadInvoiceRepository {
    LocalDateTime getEndOfLastFuelPaymentPeriod(BoatId boatId);
}
