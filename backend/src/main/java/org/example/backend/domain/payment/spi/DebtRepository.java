package org.example.backend.domain.payment.spi;

import org.example.backend.domain.payment.model.Debt;

import java.util.List;

public interface DebtRepository {
    void saveDebts(List<Debt> debts);
}
