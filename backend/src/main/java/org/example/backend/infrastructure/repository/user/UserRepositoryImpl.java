package org.example.backend.infrastructure.repository.user;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.boat.BoatId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserRepositoryImpl implements UserRepository {

    private JpaUserRepository jpaUserRepository;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public boolean existsById(Set<UserId> userIds) {
        List<User> users = jpaUserRepository.findAllById(userIds.stream().map(UserId::value).toList());
        return users.size() == userIds.size();
    }

    @Override
    public List<UserDTO> getUsersByBoatId(BoatId boatId) {
        List<User> users = jpaUserRepository.findAllByBoatId(boatId.value());
        return users.stream().map(UserMapper::toDomain).toList();
    }
}
