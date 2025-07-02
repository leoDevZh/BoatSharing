package org.example.backend.domain.User.api;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.boat.BoatId;

import java.util.List;

public interface ReadUserService {
    List<UserDTO> getUsersByBoatId(BoatId boatId);
}
