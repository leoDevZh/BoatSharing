package org.example.backend.infrastructure.controller.user;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.backend.domain.User.api.ReadUserService;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.infrastructure.controller.excpetion.ExceptionDTO;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    ReadUserService readUserService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<UserDTO> getUser() {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(UserDTO.builder().userId(new UserId(userDetail.getId())).username(userDetail.getUsername()).build());
    }

    @GetMapping(value = "all-users-boat", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created successfully, no content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<List<UserDTO>> getAllUsersByBoatId(@RequestParam BoatId boatId) {
        List<UserDTO> userDTOs = readUserService.getUsersByBoatId(boatId).stream().map(domainDTO -> UserDTO.builder()
                        .userId(domainDTO.userId())
                        .username(domainDTO.username())
                        .build())
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(userDTOs);
    }
}
