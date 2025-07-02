package org.example.backend.infrastructure.repository.payment;

import org.example.backend.infrastructure.repository.payment.model.PaymentWithUsername;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT new org.example.backend.infrastructure.repository.payment.model.PaymentWithUsername(
                p.id,
                p.paymentDate,
                p.amount,
                p.reason,
                p.isFuelPayment,
                p.status,
                p.userId,
                u.username
            )
            FROM Payment p
            JOIN User u ON p.userId = u.id
            """)
    Page<PaymentWithUsername> findAllWithUsername(Pageable pageable);
}
