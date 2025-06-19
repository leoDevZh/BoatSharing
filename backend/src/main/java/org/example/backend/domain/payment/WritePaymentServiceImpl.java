package org.example.backend.domain.payment;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.payment.api.WritePaymentService;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.DebtRepository;
import org.example.backend.domain.payment.spi.PaymentRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DomainService
public class WritePaymentServiceImpl implements WritePaymentService {

    private PaymentRepository paymentRepository;
    private DebtRepository debtRepository;
    private UserRepository userRepository;

    public WritePaymentServiceImpl(PaymentRepository paymentRepository, DebtRepository debtRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void createPayment(CreatePayment createPayment) {
        Payment payment = Payment.builder()
                .amount(createPayment.amount())
                .reason(createPayment.reason())
                .fuelPayment(createPayment.isFuelPayment())
                .status(createPayment.isFuelPayment() ? PaymentStatus.OPEN : PaymentStatus.CHARGED)
                .payedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .userId(createPayment.userId())
                .build();

        Set<UserId> userIds = new HashSet<>();
        userIds.add(createPayment.userId());
        createPayment.debts().forEach(debt -> {
            userIds.add(debt.userId());
        });
        if (!(userIds.size() == (1 + createPayment.debts().size()) && userRepository.existsById(userIds))) {
            throw new InvalidPaymentException("Provided User not valid");
        }

        PaymentId paymentId = paymentRepository.savePayment(payment);
        if (!createPayment.isFuelPayment()) {
            List<Debt> debts = createPayment.debts().stream()
                    .map(createDebt -> Debt.builder()
                            .amount(createDebt.amount())
                            .status(DebtStatus.OPEN)
                            .userId(createDebt.userId())
                            .paymentId(paymentId)
                            .build())
                    .toList();
            debtRepository.saveDebts(debts);
        }
    }
}
