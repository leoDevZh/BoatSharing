package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.payment.model.Debt;
import org.example.backend.domain.payment.spi.DebtRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DebtRepositoryImpl implements DebtRepository {

    private final JpaDebtRepository jpaDebtRepository;

    public DebtRepositoryImpl(JpaDebtRepository jpaDebtRepository) {
        this.jpaDebtRepository = jpaDebtRepository;
    }

    @Override
    public void saveDebts(List<Debt> debts) {
        this.jpaDebtRepository.saveAll(debts.stream().map(DebtMapper::toEntity).toList());
    }
}
