package org.example.backend.infrastructure.controller.boat;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.boat.spi.BoatRepository;
import org.example.backend.infrastructure.controller.excpetion.ExceptionDTO;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boat")
public class BoatController {

    @Autowired
    private BoatRepository boatRepository;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully found boat of logged in user",
                    content = @Content(schema = @Schema(implementation = BoatId.class))),
            @ApiResponse(responseCode = "400", description = "User not owner of boat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<?> getBoatFromUser() {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            BoatId boatId = boatRepository.findBoatIdByUserId(new UserId(userDetail.getId())).orElseThrow(RuntimeException::new);
            return ResponseEntity.ok(boatId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message("Could not find boat that belongs to user").build());
        }
    }
}
