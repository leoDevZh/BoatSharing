package org.example.backend.domain.payment.spi;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.DebtPaymentDTO;
import org.example.backend.domain.payment.model.DebtUserDTO;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.shared.model.PagedResult;

import java.util.List;

public interface ReadDebtRepository {

    List<DebtUserDTO> getDebtsByPaymentId(PaymentId paymentId);

    PagedResult<List<DebtPaymentDTO>> getOpenDebtsFromLoggedInUser(UserId userId, int page, int size);
}
