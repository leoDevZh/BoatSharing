package org.example.backend.domain.payment.api;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.model.CreatePayment;
import org.example.backend.domain.payment.model.DebtId;
import org.example.backend.domain.payment.model.FuelInvoiceDTO;
import org.example.backend.domain.payment.model.PaymentId;

public interface WritePaymentService {
    void createPayment(CreatePayment createPayment);

    void setDebtToPayed(UserId loggedInUser, DebtId debtId);

    void setDebtToClosed(UserId loggedInUser, DebtId debtId);

    void deletePayment(UserId loggedInUser, PaymentId paymentId);

    FuelInvoiceDTO createInvoice(BoatId boatId, UserId loggedInUser);
}
