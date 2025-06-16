package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.UserId;

import java.util.List;

public record CreatePayment(
        double amount,
        String reason,
        boolean isFuelPayment,
        UserId userId,
        List<CreateDebt> debts
) {
}
