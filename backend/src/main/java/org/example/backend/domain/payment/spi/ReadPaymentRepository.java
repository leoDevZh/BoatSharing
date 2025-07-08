package org.example.backend.domain.payment.spi;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.model.PaymentWithUsername;
import org.example.backend.domain.shared.model.PagedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadPaymentRepository {

    PagedResult<List<Payment>> getPaymentsFromLoggedInUser(UserId userId, int page, int size);

    PagedResult<List<PaymentWithUsername>> getAllPayments(int page, int size);

    Optional<Payment> getPaymentById(PaymentId paymentId);

    List<PaymentWithUsername> getFuelPaymentsForPeriod(LocalDateTime start, LocalDateTime end);
}
