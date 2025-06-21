package org.example.backend.infrastructure.repository.debt;

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
}
