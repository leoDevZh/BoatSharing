package org.example.backend.domain.payment;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.api.ReadPaymentService;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.payment.spi.ReadInvoiceRepository;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.example.backend.domain.shared.model.PagedResult;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@DomainService
public class ReadPaymentServiceImpl implements ReadPaymentService {

    private final int PAGE_SIZE = 30;

    private final ReadPaymentRepository readPaymentRepository;
    private final ReadDebtRepository readDebtRepository;
    private final ReadInvoiceRepository readInvoiceRepository;

    public ReadPaymentServiceImpl(ReadPaymentRepository readPaymentRepository, ReadDebtRepository readDebtRepository, ReadInvoiceRepository readInvoiceRepository) {
        this.readPaymentRepository = readPaymentRepository;
        this.readDebtRepository = readDebtRepository;
        this.readInvoiceRepository = readInvoiceRepository;
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
                payments.hasNext(),
                payments.totalNumber()
        );
    }

    @Override
    public PagedResult<List<PaymentUserDTO>> getAllPayments(UserDTO userDTO, int page) {
        PagedResult<List<PaymentWithUsername>> payments = readPaymentRepository.getAllPayments(page, PAGE_SIZE);

        return new PagedResult<>(
                payments.result().stream()
                        .map(payment -> {
                            List<DebtUserDTO> debts = readDebtRepository.getDebtsByPaymentId(payment.paymentId());
                            return new PaymentUserDTO(
                                    payment.paymentId(),
                                    payment.payedAt(),
                                    payment.amount(),
                                    payment.reason(),
                                    payment.isFuelPayment(),
                                    payment.paymentStatus(),
                                    payment.creditor(),
                                    debts
                            );
                        })
                        .filter(paymentUserDTO -> {
                            return paymentUserDTO.isFuelPayment() || !userDTO.username().equals("Harry");
                        })
                        .toList(),
                payments.hasNext(),
                payments.totalNumber()
        );
    }

    @Override
    public PagedResult<List<DebtPaymentDTO>> getOpenDebtsFromLoggedInUser(UserDTO user, int page) {
        return readDebtRepository.getOpenDebtsFromLoggedInUser(user.userId(), page, PAGE_SIZE);
    }

    @Override
    public PagedResult<List<DebtPaymentDTO>> getDebtsToCheck(UserDTO user, int page) {
        return readDebtRepository.getDebtsToCheck(user.userId(), page, PAGE_SIZE);
    }

    @Override
    public FuelPaymentPeriodDTO getNextFuelPaymentPeriod(BoatId boatId) {
        LocalDateTime lastEnd = this.readInvoiceRepository.getEndOfLastFuelPaymentPeriod(boatId);
        LocalDateTime nextStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        if (lastEnd != null) {
            nextStart = lastEnd.plusDays(1).with(LocalTime.MIN);
        }
        LocalDateTime nextEnd = LocalDateTime.now().minusDays(1).with(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
        return new FuelPaymentPeriodDTO(nextStart, nextEnd);
    }
}
