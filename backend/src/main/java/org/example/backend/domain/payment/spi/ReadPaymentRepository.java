package org.example.backend.domain.payment.spi;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.shared.model.PagedResult;

import java.util.List;

public interface ReadPaymentRepository {

    PagedResult<List<Payment>> getPaymentsFromLoggedInUser(UserId userId, int page, int size);
}
