package org.example.backend.infrastructure.repository.invoice;

import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.spi.WriteInvoiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WriteInvoiceRepositoryImpl implements WriteInvoiceRepository {

    private final JpaInvoiceRepository jpaInvoiceRepository;

    public WriteInvoiceRepositoryImpl(JpaInvoiceRepository jpaInvoiceRepository) {
        this.jpaInvoiceRepository = jpaInvoiceRepository;
    }


    @Override
    public void createInvoice(LocalDateTime start, LocalDateTime end, BoatId boatId) {
        this.jpaInvoiceRepository.save(Invoice.builder()
                .startDate(start)
                .endDate(end)
                .boatId(boatId.value())
                .build());
    }
}
