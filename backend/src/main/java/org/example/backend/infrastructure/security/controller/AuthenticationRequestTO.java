package org.example.backend.infrastructure.security.controller;

import lombok.Data;

@Data
public class AuthenticationRequestTO {
    private String username;
    private String password;
}
