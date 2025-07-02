package org.example.backend.infrastructure.controller.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.backend.domain.User.model.UserId;

@Data
@AllArgsConstructor
@Builder
public class UserDTO {
    @NotNull
    UserId userId;

    @NotNull
    String username;
}
