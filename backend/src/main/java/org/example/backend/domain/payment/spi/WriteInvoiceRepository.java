package org.example.backend.domain.payment.spi;

import org.example.backend.domain.payment.model.CreateInvoice;
import org.example.backend.domain.payment.model.InvoiceId;

public interface WriteInvoiceRepository {
    InvoiceId createInvoice(CreateInvoice createInvoice);
}
