package org.example.backend.infrastructure.controller.reservation;

import jakarta.validation.Valid;
import org.example.backend.domain.User.UserId;
import org.example.backend.domain.reservation.InvalidReservationException;
import org.example.backend.domain.reservation.api.ReservationService;
import org.example.backend.infrastructure.controller.excpetion.ExceptionDTO;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("create")
    public ResponseEntity create(@Valid @RequestBody CreateReservationDTO reservation) {
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
}
