package org.example.backend.infrastructure.repository.debt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaDebtRepository extends JpaRepository<Debt, Long> {

    @Query("""
                    SELECT new org.example.backend.infrastructure.repository.debt.DebtUserDTO(
                        d.id,
                        d.amount,
                        d.status,
                        d.userId,
                        u.username
                    )
                    FROM Debt d
                    JOIN User u ON u.id = d.userId
                    WHERE d.paymentId = :paymentId
            """)
    List<DebtUserDTO> findDebtsByPaymentId(@Param("paymentId") Long paymentId);

    @Query("""
                    SELECT new org.example.backend.infrastructure.repository.debt.DebtPaymentDTO(
                        p.id,
                        p.paymentDate,
                        p.reason,
                        up.id,
                        up.username,
                        d.id,
                        d.amount,
                        d.status,
                        ud.id,
                        ud.username
                    )
                    FROM Debt d
                    JOIN Payment p ON d.paymentId = p.id
                    JOIN User ud ON d.userId = ud.id
                    JOIN User up ON p.userId = up.id
                    WHERE d.userId = :debitorId
                    AND d.status = 'OPEN'
                    ORDER BY p.paymentDate DESC
            """)
    Page<DebtPaymentDTO> findOpenDebtsByUsedId(@Param("debitorId") Long debitorId, Pageable pageable);

    @Query("""
                    SELECT new org.example.backend.infrastructure.repository.debt.DebtPaymentDTO(
                        p.id,
                        p.paymentDate,
                        p.reason,
                        up.id,
                        up.username,
                        d.id,
                        d.amount,
                        d.status,
                        ud.id,
                        ud.username
                    )
                    FROM Debt d
                    JOIN Payment p ON d.paymentId = p.id
                    JOIN User ud ON d.userId = ud.id
                    JOIN User up ON p.userId = up.id
                    WHERE p.userId = :creditorId
                    AND d.status = 'PAYED'
                    ORDER BY p.paymentDate DESC
            """)
    Page<DebtPaymentDTO> findDebtsToCheckByUsedId(@Param("creditorId") Long creditorId, Pageable pageable);
}
