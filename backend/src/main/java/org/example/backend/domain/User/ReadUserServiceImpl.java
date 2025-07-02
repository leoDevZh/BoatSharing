package org.example.backend.domain.User;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.api.ReadUserService;
import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.boat.BoatId;

import java.util.List;

@DomainService
public class ReadUserServiceImpl implements ReadUserService {

    private UserRepository userRepository;

    public ReadUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getUsersByBoatId(BoatId boatId) {
        return this.userRepository.getUsersByBoatId(boatId);
    }
}
