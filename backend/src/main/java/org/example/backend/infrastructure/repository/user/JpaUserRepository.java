package org.example.backend.infrastructure.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = """
                SELECT u.* FROM users u 
                JOIN users_boats ub ON u.id = ub.user_id 
                WHERE ub.boat_id = :boatId
            """, nativeQuery = true)
    List<User> findAllByBoatId(@Param("boatId") Long boatId);
}
