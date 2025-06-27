package org.example.backend.domain.payment;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.payment.api.WritePaymentService;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.DebtRepository;
import org.example.backend.domain.payment.spi.PaymentRepository;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.springframework.transaction.annotation.Transactional;

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
    private ReadDebtRepository readDebtRepository;
    private ReadPaymentRepository readPaymentRepository;

    public WritePaymentServiceImpl(PaymentRepository paymentRepository, DebtRepository debtRepository, UserRepository userRepository, ReadDebtRepository readDebtRepository, ReadPaymentRepository readPaymentRepository) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.readDebtRepository = readDebtRepository;
        this.readPaymentRepository = readPaymentRepository;
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

    @Override
    public void setDebtToPayed(UserId loggedInUser, DebtId debtId) {
        Debt debt = readDebtRepository.getDebtById(debtId).orElseThrow(() -> new InvalidPaymentException("Debt not found"));
        if (!debt.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Debt does not belong to User");
        }
        debt.setPayed();
        debtRepository.saveDebts(List.of(debt));
    }

    @Transactional
    @Override
    public void setDebtToClosed(UserId loggedInUser, DebtId debtId) {
        Debt debt = readDebtRepository.getDebtById(debtId).orElseThrow(() -> new InvalidPaymentException("Debt not found"));
        Payment payment = readPaymentRepository.getPaymentById(debt.getPaymentId()).orElseThrow(() -> new InvalidPaymentException("Payment not found"));
        List<DebtUserDTO> debts = readDebtRepository.getDebtsByPaymentId(payment.getPaymentId());
        if (!payment.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Payment does not belong to User");
        }
        debt.setClosed();
        debtRepository.saveDebts(List.of(debt));

        boolean updatePayment = true;
        for (DebtUserDTO debtUserDTO : debts) {
            if (debtUserDTO.debtId().equals(debtId)) {
                continue;
            }
            if (debtUserDTO.debtStatus() != DebtStatus.CLOSED) {
                updatePayment = false;
            }
        }
        if (updatePayment) {
            payment.setClosed();
            paymentRepository.savePayment(payment);
        }
    }

    @Override
    public void deletePayment(UserId loggedInUser, PaymentId paymentId) {
        Payment payment = readPaymentRepository.getPaymentById(paymentId).orElseThrow(() -> new InvalidPaymentException("Payment not found"));
        if (!payment.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Payment does not belong to User");
        }
        paymentRepository.deletePayment(paymentId);
    }
}
