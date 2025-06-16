package org.example.backend.infrastructure.repository.debt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDebtRepository extends JpaRepository<Debt, Long> {
}
