package org.example.backend.infrastructure.controller.payment;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.api.ReadPaymentService;
import org.example.backend.domain.payment.model.PaymentUserDTO;
import org.example.backend.domain.shared.model.PagedResult;
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
@RequestMapping("/api/read-payment")
public class ReadPaymentController {

    @Autowired
    private ReadPaymentService readPaymentService;

    @GetMapping(value = "all-from-logged-user", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get all payments from logged in user paged"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<PagedResult<List<PaymentUserDTO>>> getPaymentsFromLoggedInUser(@RequestParam int page) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PagedResult<List<PaymentUserDTO>> data = readPaymentService.getPaymentsFromLoggedInUser(new UserDTO(new UserId(userDetail.getId()), userDetail.getUsername()), page);
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

    @GetMapping(value = "all", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get all payments paged"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<PagedResult<List<PaymentUserDTO>>> getAllPayments(@RequestParam int page) {
        PagedResult<List<PaymentUserDTO>> data = readPaymentService.getAllPayments(page);
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }
}
