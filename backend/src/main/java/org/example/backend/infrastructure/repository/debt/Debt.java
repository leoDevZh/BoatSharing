package org.example.backend.infrastructure.repository.debt;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.payment.model.DebtStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "debts")
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @Column(name = "payment_ID", nullable = false)
    private Long paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
