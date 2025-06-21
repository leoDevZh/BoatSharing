package org.example.backend.infrastructure.repository.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByUserId(Long userId, Pageable pageable);
}
