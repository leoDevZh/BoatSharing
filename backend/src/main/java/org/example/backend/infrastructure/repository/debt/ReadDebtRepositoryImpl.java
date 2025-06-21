package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.payment.model.DebtUserDTO;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReadDebtRepositoryImpl implements ReadDebtRepository {

    private final JpaDebtRepository jpaDebtRepository;

    public ReadDebtRepositoryImpl(JpaDebtRepository jpaDebtRepository) {
        this.jpaDebtRepository = jpaDebtRepository;
    }

    @Override
    public List<DebtUserDTO> getDebtsByPaymentId(PaymentId paymentId) {
        return jpaDebtRepository.findDebtsByPaymentId(paymentId.value()).stream()
                .map(DebtMapper::toDebtUserDTODomain)
                .toList();
    }
}
