package org.example.backend.domain.boat.spi;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;

import java.util.Optional;

public interface BoatRepository {
    Optional<Boat> findByIdWithOwners(BoatId id);

    Optional<BoatId> findBoatIdByUserId(UserId userId);
}
