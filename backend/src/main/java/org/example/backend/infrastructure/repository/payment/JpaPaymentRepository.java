package org.example.backend.infrastructure.repository.payment;

import org.example.backend.infrastructure.repository.payment.model.PaymentWithUsername;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
                    WHERE p.isFuelPayment = true
                    AND p.paymentDate >= :from
                    AND p.paymentDate <= :to
                    AND p.status = 'OPEN'
            """)
    List<PaymentWithUsername> findFuelPaymentsForPeriod(@Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);

    @Modifying
    @Query("""
            UPDATE Payment p
            SET p.status = 'CLOSED'
            WHERE p.id = :paymentId
            """)
    void setPaymentToClosed(@Param("paymentId") Long paymentId);
}
