package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.DebtPaymentDTO;
import org.example.backend.domain.payment.model.DebtUserDTO;
import org.example.backend.domain.payment.model.PaymentId;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.shared.model.PagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public PagedResult<List<DebtPaymentDTO>> getOpenDebtsFromLoggedInUser(UserId userId, int page, int size) {
        Page<org.example.backend.infrastructure.repository.debt.DebtPaymentDTO> debts = jpaDebtRepository.findOpenDebtsByUsedId(userId.value(), PageRequest.of(page, size));
        return new PagedResult<>(
                debts.get().map(
                        DebtMapper::toDebtPaymentDTODomain
                ).toList(),
                debts.getTotalPages() != (page + 1),
                debts.getTotalElements()
        );
    }

    @Override
    public PagedResult<List<DebtPaymentDTO>> getDebtsToCheck(UserId creditorId, int page, int size) {
        Page<org.example.backend.infrastructure.repository.debt.DebtPaymentDTO> debts = jpaDebtRepository.findDebtsToCheckByUsedId(creditorId.value(), PageRequest.of(page, size));
        return new PagedResult<>(
                debts.get().map(
                        DebtMapper::toDebtPaymentDTODomain
                ).toList(),
                debts.getTotalPages() != (page + 1),
                debts.getTotalElements()
        );
    }
}
