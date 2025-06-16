package org.example.backend.infrastructure.repository.payment;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.payment.model.PaymentStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "is_fuel_payment", nullable = false)
    private Boolean isFuelPayment;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
