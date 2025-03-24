package org.example.backend.domain.boat.spi;

import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoatRepository {
    Optional<Boat> findByIdWithOwners(BoatId id);
}
