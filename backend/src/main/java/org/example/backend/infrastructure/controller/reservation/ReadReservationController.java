package org.example.backend.infrastructure.controller.reservation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.api.ReadReservationService;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/read-reservation")
public class ReadReservationController {

    @Autowired
    private ReadReservationService readReservationService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservationUserDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            )
    })
    public ResponseEntity<List<ReservationUserDTO>> findReservationForPeriod(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to, @RequestParam BoatId boatId) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReservationUserDTO> reservationUserDTOS = readReservationService.getReservationForPeriod(from, to, boatId, new UserId(userDetail.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(reservationUserDTOS);
    }
}
