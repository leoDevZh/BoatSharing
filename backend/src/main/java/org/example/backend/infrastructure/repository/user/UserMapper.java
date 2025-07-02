package org.example.backend.infrastructure.repository.user;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;

public class UserMapper {
    static UserDTO toDomain(User user) {
        return new UserDTO(new UserId(user.getId()), user.getUsername());
    }
}
