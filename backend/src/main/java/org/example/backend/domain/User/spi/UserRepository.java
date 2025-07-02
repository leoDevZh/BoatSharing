package org.example.backend.domain.User.spi;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;

import java.util.List;
import java.util.Set;

public interface UserRepository {
    boolean existsById(Set<UserId> userIds);

    List<UserDTO> getUsersByBoatId(BoatId boatId);
}
