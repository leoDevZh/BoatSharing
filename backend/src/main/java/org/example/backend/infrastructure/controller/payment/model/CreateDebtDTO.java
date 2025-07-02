package org.example.backend.infrastructure.controller.payment.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.domain.User.model.UserId;

@Data
@AllArgsConstructor
public class CreateDebtDTO {
    @NotNull
    double amount;

    @NotNull
    UserId userId;
}
