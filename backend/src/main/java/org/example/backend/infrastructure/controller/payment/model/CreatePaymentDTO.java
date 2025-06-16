package org.example.backend.infrastructure.controller.payment.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentDTO {
    @NotNull
    Double amount;

    @NotNull
    String reason;

    @NotNull
    Boolean isFuelPayment;

    List<CreateDebtDTO> debts;

    public CreatePaymentDTO(double amount, String reason, boolean isFuelPayment) {
        this.amount = amount;
        this.reason = reason;
        this.isFuelPayment = isFuelPayment;
    }
}
