package org.example.backend.infrastructure.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponseTO {
    private String token;
}
