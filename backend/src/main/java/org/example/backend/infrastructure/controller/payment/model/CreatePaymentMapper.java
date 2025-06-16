package org.example.backend.infrastructure.controller.payment.model;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.payment.model.CreateDebt;
import org.example.backend.domain.payment.model.CreatePayment;

import java.util.Collections;
import java.util.Optional;

public class CreatePaymentMapper {

    public static CreatePayment toCreatePayment(CreatePaymentDTO createPaymentDTO, UserId loggedInUserId) {
        return new CreatePayment(
                createPaymentDTO.getAmount(),
                createPaymentDTO.getReason(),
                createPaymentDTO.getIsFuelPayment(),
                loggedInUserId,
                Optional.ofNullable(createPaymentDTO.getDebts())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(createDebtDTO -> new CreateDebt(createDebtDTO.getAmount(), createDebtDTO.getUserId()))
                        .toList()
        );
    }
}
