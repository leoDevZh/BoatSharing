package org.example.backend.infrastructure.controller.payment;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.api.WritePaymentService;
import org.example.backend.domain.payment.model.CreatePayment;
import org.example.backend.domain.payment.model.DebtId;
import org.example.backend.infrastructure.controller.excpetion.ExceptionDTO;
import org.example.backend.infrastructure.controller.payment.model.CreatePaymentDTO;
import org.example.backend.infrastructure.controller.payment.model.CreatePaymentMapper;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class WritePaymentController {

    @Autowired
    private WritePaymentService writePaymentService;

    @PostMapping(value = "create", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created successfully, no content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid payment",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<ExceptionDTO> create(@Valid @RequestBody CreatePaymentDTO createPaymentDTO) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreatePayment createPayment = CreatePaymentMapper.toCreatePayment(createPaymentDTO, new UserId(userDetail.getId()));
        writePaymentService.createPayment(createPayment);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "debt-to-payed/{debtId}", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Set debt status to payed"),
            @ApiResponse(responseCode = "400", description = "Invalid debt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<ExceptionDTO> setDebtToPayed(@PathVariable(value = "debtId") DebtId debtId) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        writePaymentService.setDebtToPayed(new UserId(userDetail.getId()), debtId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(value = "debt-to-closed/{debtId}", produces = "application/json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Set debt status to closed"),
            @ApiResponse(responseCode = "400", description = "Invalid debt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDTO.class)))
    })
    public ResponseEntity<ExceptionDTO> setDebtToClosed(@PathVariable(value = "debtId") DebtId debtId) {
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        writePaymentService.setDebtToClosed(new UserId(userDetail.getId()), debtId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
