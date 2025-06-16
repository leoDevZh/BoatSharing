package org.example.backend.domain.User.spi;

import org.example.backend.domain.User.UserId;

import java.util.Set;

public interface UserRepository {
    boolean existsById(Set<UserId> userIds);
}
