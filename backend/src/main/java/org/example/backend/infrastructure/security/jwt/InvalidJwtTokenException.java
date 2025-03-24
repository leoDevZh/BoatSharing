package org.example.backend.infrastructure.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtTokenException extends AuthenticationException {
    public InvalidJwtTokenException(String msg) {
        super(msg);
    }
}
