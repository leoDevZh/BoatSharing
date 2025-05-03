package org.example.backend.infrastructure.controller.user;

import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping(produces = "application/json")
    public ResponseEntity<UserDTO> getUser() {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(UserDTO.builder().username(userDetail.getUsername()).build());
    }
}
