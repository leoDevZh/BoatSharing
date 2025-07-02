package org.example.backend.infrastructure.repository.boat;

import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.Boat;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BoatRepositoryImplementation implements BoatRepository {

    private final JpaBoatRepository jpaBoatRepository;

    public BoatRepositoryImplementation(JpaBoatRepository jpaBoatRepository) {
        this.jpaBoatRepository = jpaBoatRepository;
    }

    public Optional<BoatId> findBoatIdByUserId(UserId userId) {
        return jpaBoatRepository.findBoatByUserIdContains(userId.value()).map(boat -> new BoatId(boat.getId()));
    }

    @Override
    public Optional<Boat> findByIdWithOwners(BoatId id) {
        Optional<org.example.backend.infrastructure.repository.boat.Boat> boatOptional = jpaBoatRepository.findByIdWithOwners(id.value());
        return boatOptional.map(BoatMapper::toDomain);
    }

    static class BoatMapper {
        static Boat toDomain(org.example.backend.infrastructure.repository.boat.Boat boat) {
            return new Boat(
                    new BoatId(boat.getId()),
                    boat.getName(),
                    boat.getUserIds().stream().map(UserId::new).collect(Collectors.toSet())
            );
        }
    }
}
