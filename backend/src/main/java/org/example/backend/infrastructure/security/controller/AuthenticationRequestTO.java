package org.example.backend.infrastructure.security.controller;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthenticationRequestTO {
    @NotNull
    private String username;
    @NotNull
    private String password;
}
