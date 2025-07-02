package org.example.backend.domain.payment.spi;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.shared.model.PagedResult;

import java.util.List;
import java.util.Optional;

public interface ReadDebtRepository {

    List<DebtUserDTO> getDebtsByPaymentId(PaymentId paymentId);

    PagedResult<List<DebtPaymentDTO>> getOpenDebtsFromLoggedInUser(UserId debitorId, int page, int size);

    PagedResult<List<DebtPaymentDTO>> getDebtsToCheck(UserId creditorId, int page, int size);

    Optional<Debt> getDebtById(DebtId debtId);
}
