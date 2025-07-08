package org.example.backend.domain.payment.model;

import org.example.backend.domain.User.model.UserDTO;

public record UserTotalPayedDTO(
        Double totalPayed,
        UserDTO userDTO
) {
}
