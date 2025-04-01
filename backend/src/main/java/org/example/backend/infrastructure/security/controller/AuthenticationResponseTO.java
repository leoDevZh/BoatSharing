package org.example.backend.infrastructure.security.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponseTO {
    @NotNull
    private String token;
}
