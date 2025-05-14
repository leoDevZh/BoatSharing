package org.example.backend.infrastructure.controller.reservation;

import org.example.backend.domain.User.UserId;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.reservation.api.ReadReservationService;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
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
    public ResponseEntity<List<ReservationUserDTO>> findReservationForPeriod(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to, @RequestParam BoatId boatId) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReservationUserDTO> reservationUserDTOS = readReservationService.getReservationForPeriod(from, to, boatId, new UserId(userDetail.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(reservationUserDTOS);
    }
}
