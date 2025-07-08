package org.example.backend.domain.payment.model;

import java.util.List;

public record FuelInvoiceDTO(
        FuelPaymentPeriodDTO fuelPaymentPeriodDTO,
        Double totalHours,
        Double totalPayed,
        List<UserTotalHoursDTO> userTotalHoursDTOs,
        List<UserTotalPayedDTO> userTotalPayedDTOs,
        List<PaymentId> paymentsCharged
) {
}
