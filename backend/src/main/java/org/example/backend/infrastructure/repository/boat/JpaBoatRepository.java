package org.example.backend.infrastructure.repository.boat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaBoatRepository extends JpaRepository<Boat, Long> {
    @Query("SELECT b FROM Boat b LEFT JOIN FETCH b.userIds WHERE b.id = :id")
    Optional<Boat> findByIdWithOwners(@Param("id") Long id);
}
