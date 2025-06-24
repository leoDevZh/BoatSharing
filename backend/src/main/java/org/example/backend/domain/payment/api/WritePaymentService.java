package org.example.backend.domain.payment.api;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.CreatePayment;
import org.example.backend.domain.payment.model.DebtId;

public interface WritePaymentService {
    void createPayment(CreatePayment createPayment);

    void setDebtToPayed(UserId loggedInUser, DebtId debtId);
}
