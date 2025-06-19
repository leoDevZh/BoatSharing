package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.InvalidPaymentException;

import java.time.LocalDateTime;

public class Payment {
    private PaymentId paymentId;
    private LocalDateTime payedAt;
    private double amount;
    private String reason;
    private boolean isFuelPayment;
    private PaymentStatus status;
    private UserId userId;

    public Payment(PaymentBuilder paymentBuilder) {
        if (paymentBuilder.payedAt == null || paymentBuilder.amount <= 0 || paymentBuilder.reason == null || paymentBuilder.reason.isEmpty() || paymentBuilder.userId == null || paymentBuilder.status == null) {
            throw new InvalidPaymentException("Invalid Payment values provided");
        }
        this.paymentId = paymentBuilder.paymentId;
        this.payedAt = paymentBuilder.payedAt;
        this.amount = paymentBuilder.amount;
        this.isFuelPayment = paymentBuilder.isFuelPayment;
        this.reason = paymentBuilder.reason;
        this.status = paymentBuilder.status;
        this.userId = paymentBuilder.userId;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public UserId getUserId() {
        return userId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPayedAt() {
        return payedAt;
    }

    public boolean isFuelPayment() {
        return isFuelPayment;
    }

    public static class PaymentBuilder {
        PaymentId paymentId;
        LocalDateTime payedAt;
        double amount;
        String reason;
        boolean isFuelPayment;
        PaymentStatus status;
        UserId userId;

        public Payment build() {
            return new Payment(this);
        }

        private PaymentBuilder() {
        }

        public PaymentBuilder paymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public PaymentBuilder payedAt(LocalDateTime payedAt) {
            this.payedAt = payedAt;
            return this;
        }

        public PaymentBuilder userId(UserId userId) {
            this.userId = userId;
            return this;
        }

        public PaymentBuilder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public PaymentBuilder fuelPayment(boolean fuelPayment) {
            isFuelPayment = fuelPayment;
            return this;
        }

        public PaymentBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public PaymentBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }
    }
}
