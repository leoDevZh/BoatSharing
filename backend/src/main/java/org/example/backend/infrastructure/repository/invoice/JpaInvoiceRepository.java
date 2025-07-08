package org.example.backend.infrastructure.repository.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query(
            """
                    SELECT i FROM Invoice i WHERE i.boatId = :boatId ORDER BY i.endDate DESC LIMIT 1
                    """
    )
    Optional<Invoice> findLatestInvoice(@Param("boatId") Long boatId);
}
