package org.example.backend.domain.payment.api;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.payment.model.PaymentUserDTO;
import org.example.backend.domain.shared.model.PagedResult;

import java.util.List;

public interface ReadPaymentService {
    PagedResult<List<PaymentUserDTO>> getPaymentsFromLoggedInUser(UserDTO user, int page);
}
