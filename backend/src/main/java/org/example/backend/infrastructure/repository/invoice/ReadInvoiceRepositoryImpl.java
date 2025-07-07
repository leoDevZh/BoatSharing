package org.example.backend.infrastructure.repository.invoice;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.spi.ReadInvoiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ReadInvoiceRepositoryImpl implements ReadInvoiceRepository {

    private final JpaInvoiceRepository jpaInvoiceRepository;

    public ReadInvoiceRepositoryImpl(JpaInvoiceRepository jpaInvoiceRepository) {
        this.jpaInvoiceRepository = jpaInvoiceRepository;
    }

    @Override
    public LocalDateTime getEndOfLastFuelPaymentPeriod(BoatId boatId) {
        Optional<Invoice> lastInvoice = this.jpaInvoiceRepository.findLatestInvoice(boatId.value());
        return lastInvoice.map(Invoice::getEndDate).orElse(null);
    }
}
