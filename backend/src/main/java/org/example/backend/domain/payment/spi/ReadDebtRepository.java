package org.example.backend.domain.payment.spi;

import org.example.backend.domain.payment.model.DebtUserDTO;
import org.example.backend.domain.payment.model.PaymentId;

import java.util.List;

public interface ReadDebtRepository {

    List<DebtUserDTO> getDebtsByPaymentId(PaymentId paymentId);

}
