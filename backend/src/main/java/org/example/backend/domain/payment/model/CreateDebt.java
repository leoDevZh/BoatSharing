package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.UserId;

public record CreateDebt(
        double amount,
        UserId userId
) {
}
