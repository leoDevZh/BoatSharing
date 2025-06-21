package org.example.backend.domain.payment;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.payment.api.ReadPaymentService;
import org.example.backend.domain.payment.model.DebtUserDTO;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentUserDTO;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.example.backend.domain.shared.model.PagedResult;

import java.util.List;

@DomainService
public class ReadPaymentServiceImpl implements ReadPaymentService {

    private final int PAGE_SIZE = 30;

    private ReadPaymentRepository readPaymentRepository;
    private ReadDebtRepository readDebtRepository;

    public ReadPaymentServiceImpl(ReadPaymentRepository readPaymentRepository, ReadDebtRepository readDebtRepository) {
        this.readPaymentRepository = readPaymentRepository;
        this.readDebtRepository = readDebtRepository;
    }

    @Override
    public PagedResult<List<PaymentUserDTO>> getPaymentsFromLoggedInUser(UserDTO user, int page) {
        PagedResult<List<Payment>> payments = readPaymentRepository.getPaymentsFromLoggedInUser(user.userId(), page, PAGE_SIZE);

        return new PagedResult<>(
                payments.result().stream()
                        .map(payment -> {
                            List<DebtUserDTO> debts = readDebtRepository.getDebtsByPaymentId(payment.getPaymentId());
                            return new PaymentUserDTO(
                                    payment.getPaymentId(),
                                    payment.getPayedAt(),
                                    payment.getAmount(),
                                    payment.getReason(),
                                    payment.isFuelPayment(),
                                    payment.getStatus(),
                                    user,
                                    debts
                            );
                        }).toList(),
                payments.hasNext()
        );
    }
}
