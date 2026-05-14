package org.example.backend.infrastructure.repository.invoice;

import org.example.backend.domain.payment.model.CreateInvoice;
import org.example.backend.domain.payment.model.InvoiceId;
import org.example.backend.domain.payment.spi.WriteInvoiceRepository;
import org.springframework.stereotype.Component;

@Component
public class WriteInvoiceRepositoryImpl implements WriteInvoiceRepository {

    private final JpaInvoiceRepository jpaInvoiceRepository;
    private final JpaInvoiceUserHoursRepository jpaInvoiceUserHoursRepository;
    private final JpaInvoiceUserPayedRepository jpaInvoiceUserPayedRepository;

    public WriteInvoiceRepositoryImpl(JpaInvoiceRepository jpaInvoiceRepository,
                                      JpaInvoiceUserHoursRepository jpaInvoiceUserHoursRepository,
                                      JpaInvoiceUserPayedRepository jpaInvoiceUserPayedRepository) {
        this.jpaInvoiceRepository = jpaInvoiceRepository;
        this.jpaInvoiceUserHoursRepository = jpaInvoiceUserHoursRepository;
        this.jpaInvoiceUserPayedRepository = jpaInvoiceUserPayedRepository;
    }

    @Override
    public InvoiceId createInvoice(CreateInvoice createInvoice) {
        Invoice saved = jpaInvoiceRepository.save(Invoice.builder()
                .startDate(createInvoice.period().startDate())
                .endDate(createInvoice.period().endDate())
                .boatId(createInvoice.boatId().value())
                .totalHours(createInvoice.totalHours())
                .totalPayed(createInvoice.totalPayed())
                .build());

        Long invoiceId = saved.getId();

        jpaInvoiceUserHoursRepository.saveAll(
                createInvoice.userHours().stream()
                        .map(u -> InvoiceUserHours.builder()
                                .invoiceId(invoiceId)
                                .userId(u.userDTO().userId().value())
                                .totalHours(u.totalHours())
                                .build())
                        .toList());

        jpaInvoiceUserPayedRepository.saveAll(
                createInvoice.userPayed().stream()
                        .map(u -> InvoiceUserPayed.builder()
                                .invoiceId(invoiceId)
                                .userId(u.userDTO().userId().value())
                                .totalPayed(u.totalPayed())
                                .build())
                        .toList());

        return new InvoiceId(invoiceId);
    }
}