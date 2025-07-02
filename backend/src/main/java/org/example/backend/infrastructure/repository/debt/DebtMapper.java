package org.example.backend.infrastructure.repository.debt;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.DebtId;
import org.example.backend.domain.payment.model.PaymentId;

public class DebtMapper {

    static Debt toEntity(org.example.backend.domain.payment.model.Debt debt) {
        Long id = debt.getDebtId() == null ? null : debt.getDebtId().value();
        return Debt.builder()
                .id(id)
                .amount(debt.getAmount())
                .status(debt.getStatus())
                .paymentId(debt.getPaymentId().value())
                .userId(debt.getUserId().value())
                .build();
    }

    static org.example.backend.domain.payment.model.Debt toDomain(Debt debt) {
        return org.example.backend.domain.payment.model.Debt.builder()
                .debtId(new DebtId(debt.getId()))
                .amount(debt.getAmount())
                .status(debt.getStatus())
                .paymentId(new PaymentId(debt.getPaymentId()))
                .userId(new UserId(debt.getUserId()))
                .build();
    }

    static org.example.backend.domain.payment.model.DebtUserDTO toDebtUserDTODomain(DebtUserDTO dto) {
        return new org.example.backend.domain.payment.model.DebtUserDTO(
                new DebtId(dto.debtId()),
                dto.amount(),
                dto.debtStatus(),
                new UserDTO(
                        new UserId(dto.userId()),
                        dto.username()
                )
        );
    }

    static org.example.backend.domain.payment.model.DebtPaymentDTO toDebtPaymentDTODomain(DebtPaymentDTO dto) {
        return new org.example.backend.domain.payment.model.DebtPaymentDTO(
                new PaymentId(dto.paymentId()),
                dto.payedAt(),
                dto.reason(),
                new UserDTO(new UserId(dto.creditorId()), dto.creditorUsername()),
                new org.example.backend.domain.payment.model.DebtUserDTO(
                        new DebtId(dto.debtId()),
                        dto.amount(),
                        dto.debtStatus(),
                        new UserDTO(
                                new UserId(dto.debitorUserId()),
                                dto.debitorUsername()
                        )
                )
        );
    }
}
