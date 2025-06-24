package org.example.backend.infrastructure.repository.payment;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.Payment;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.model.PaymentWithUsername;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.example.backend.domain.shared.model.PagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Component
public class ReadPaymentRepositoryImpl implements ReadPaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    public ReadPaymentRepositoryImpl(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }


    @Override
    public PagedResult<List<Payment>> getPaymentsFromLoggedInUser(UserId userId, int page, int size) {
        Page<org.example.backend.infrastructure.repository.payment.Payment> payments = jpaPaymentRepository.findAllByUserId(userId.value(), PageRequest.of(page, size, Sort.by(DESC, "paymentDate")));
        return new PagedResult<>(
                payments.get()
                        .map(PaymentMapper::toDomain)
                        .toList(),
                payments.getTotalPages() != (page + 1),
                payments.getTotalElements()
        );
    }

    @Override
    public PagedResult<List<PaymentWithUsername>> getAllPayments(int page, int size) {
        Page<org.example.backend.infrastructure.repository.payment.model.PaymentWithUsername> payments = jpaPaymentRepository.findAllWithUsername(PageRequest.of(page, size, Sort.by(DESC, "paymentDate")));
        return new PagedResult<>(
                payments.get()
                        .map(PaymentMapper::toDomain)
                        .toList(),
                payments.getTotalPages() != (page + 1),
                payments.getTotalElements()
        );
    }

    @Override
    public Optional<Payment> getPaymentById(PaymentId paymentId) {
        return jpaPaymentRepository.findById(paymentId.value()).map(PaymentMapper::toDomain);
    }
}
