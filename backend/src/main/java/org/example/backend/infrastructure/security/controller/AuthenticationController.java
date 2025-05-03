package org.example.backend.infrastructure.security.controller;

import org.example.backend.infrastructure.security.jwt.JwtUtil;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    public AuthenticationController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<AuthenticationResponseTO> authenticate(@RequestBody AuthenticationRequestTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = JwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new AuthenticationResponseTO(token));
    }

    @PostMapping(value = "/refresh", produces = "application/json")
    public ResponseEntity<AuthenticationResponseTO> refresh() {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String token = JwtUtil.generateToken(userDetail.getUsername());
        return ResponseEntity.ok(new AuthenticationResponseTO(token));
    }
}