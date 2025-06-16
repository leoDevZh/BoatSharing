package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.payment.InvalidPaymentException;

public class Debt {
    private DebtId debtId;
    private double amount;
    private DebtStatus status;
    private PaymentId paymentId;
    private UserId userId;

    public Debt(DebtBuilder debtBuilder) {
        if (debtBuilder.amount <= 0 || debtBuilder.status == null || debtBuilder.paymentId == null || debtBuilder.userId == null) {
            throw new InvalidPaymentException("Invalid Debt provided. Values must not be null");
        }
        this.debtId = debtBuilder.debtId;
        this.amount = debtBuilder.amount;
        this.status = debtBuilder.status;
        this.paymentId = debtBuilder.paymentId;
        this.userId = debtBuilder.userId;
    }

    public static DebtBuilder builder() {
        return new DebtBuilder();
    }

    public DebtId getDebtId() {
        return debtId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public UserId getUserId() {
        return userId;
    }

    public DebtStatus getStatus() {
        return status;
    }

    public static class DebtBuilder {
        DebtId debtId;
        double amount;
        DebtStatus status;
        PaymentId paymentId;
        UserId userId;

        private DebtBuilder() {
        }

        public Debt build() {
            return new Debt(this);
        }

        public DebtBuilder debtId(DebtId debtId) {
            this.debtId = debtId;
            return this;
        }

        public DebtBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public DebtBuilder status(DebtStatus status) {
            this.status = status;
            return this;
        }

        public DebtBuilder paymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public DebtBuilder userId(UserId userId) {
            this.userId = userId;
            return this;
        }
    }
}
