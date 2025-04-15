package org.example.backend.infrastructure.controller.reservation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.reservation.InvalidReservationException;
import org.example.backend.domain.reservation.ReservationId;
import org.example.backend.domain.reservation.api.ReservationService;
import org.example.backend.infrastructure.controller.excpetion.ExceptionDTO;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping(value = "create", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created successfully, no content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<ExceptionDTO> create(@Valid @RequestBody CreateReservationDTO reservation) {
        try {
            CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reservationService.makeNewReservation(new UserId(userDetail.getId()), reservation.boatId, reservation.startTime, reservation.endTime);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidReservationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message(e.getMessage()).build());
        }
    }

    @PostMapping(value = "updateEngineHours", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully, no content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<ExceptionDTO> updateEngineHours(@Valid @RequestBody UpdateEngineHoursDTO reservation) {
        try {
            CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reservationService.updateEngineHoursForReservation(new UserId(userDetail.getId()), reservation.reservationId, reservation.boatEngineHoursOnStart, reservation.boatEngineHoursOnEnd);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidReservationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message(e.getMessage()).build());
        }
    }

    @DeleteMapping(value = "cancel/{reservationId}", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully, no content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity cancelReservation(@PathVariable(value = "reservationId") ReservationId reservationId) {
        try {
            CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reservationService.cancelReservation(new UserId(userDetail.getId()), reservationId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (InvalidReservationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message(e.getMessage()).build());
        }
    }
}
