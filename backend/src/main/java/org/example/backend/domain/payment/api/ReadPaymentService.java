package org.example.backend.domain.payment.api;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.model.DebtPaymentDTO;
import org.example.backend.domain.payment.model.FuelPaymentPeriodDTO;
import org.example.backend.domain.payment.model.PaymentUserDTO;
import org.example.backend.domain.shared.model.PagedResult;
import org.example.backend.infrastructure.repository.user.User;

import java.util.List;

public interface ReadPaymentService {
    PagedResult<List<PaymentUserDTO>> getPaymentsFromLoggedInUser(UserDTO user, int page);

    PagedResult<List<PaymentUserDTO>> getAllPayments(UserDTO user, int page);

    PagedResult<List<DebtPaymentDTO>> getOpenDebtsFromLoggedInUser(UserDTO user, int page);

    PagedResult<List<DebtPaymentDTO>> getDebtsToCheck(UserDTO user, int page);

    FuelPaymentPeriodDTO getNextFuelPaymentPeriod(BoatId boatId);
}
