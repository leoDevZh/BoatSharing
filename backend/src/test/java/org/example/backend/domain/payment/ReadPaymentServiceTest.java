package org.example.backend.domain.payment;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.example.backend.domain.shared.model.PagedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.backend.domain.payment.model.PaymentStatus.OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ReadPaymentServiceTest {

    @Mock
    private ReadPaymentRepository readPaymentRepository;

    @Mock
    private ReadDebtRepository readDebtRepository;

    @InjectMocks
    private ReadPaymentServiceImpl readPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class GetAllPaymentsFromLoggedInUser {
        @Test
        void shouldReadPaymentFromLoggedInUserSuccessfully() {
            UserDTO loggedInUser = new UserDTO(new UserId(1L), "user");
            int page = 0;
            Payment payment = Payment.builder()
                    .paymentId(new PaymentId(1L))
                    .payedAt(LocalDateTime.now())
                    .amount(10)
                    .fuelPayment(false)
                    .reason("reason")
                    .status(OPEN)
                    .userId(loggedInUser.userId())
                    .build();
            DebtUserDTO debtUserDTO = new DebtUserDTO(new DebtId(1L), 10., DebtStatus.OPEN, loggedInUser);
            PaymentUserDTO expected = new PaymentUserDTO(
                    payment.getPaymentId(),
                    payment.getPayedAt(),
                    payment.getAmount(),
                    payment.getReason(),
                    payment.isFuelPayment(),
                    payment.getStatus(),
                    loggedInUser,
                    List.of(debtUserDTO)
            );
            when(readPaymentRepository.getPaymentsFromLoggedInUser(loggedInUser.userId(), page, 30)).thenReturn(new PagedResult<>(List.of(payment), true, 1));
            when(readDebtRepository.getDebtsByPaymentId(payment.getPaymentId())).thenReturn(List.of(debtUserDTO));

            PagedResult<List<PaymentUserDTO>> actual = readPaymentService.getPaymentsFromLoggedInUser(loggedInUser, page);

            assertTrue(actual.hasNext());
            assertEquals(1, actual.result().size());
            assertEquals(expected, actual.result().get(0));
        }
    }

    @Nested
    class GetAllPayments {
        @Test
        void shouldReadPaymentSuccessfully() {
            UserDTO user = new UserDTO(new UserId(1L), "user");
            int page = 0;
            PaymentWithUsername payment = new PaymentWithUsername(
                    new PaymentId(1L),
                    LocalDateTime.now(),
                    10.,
                    "reason",
                    false,
                    OPEN,
                    user
            );
            DebtUserDTO debtUserDTO = new DebtUserDTO(new DebtId(1L), 10., DebtStatus.OPEN, user);
            PaymentUserDTO expected = new PaymentUserDTO(
                    payment.paymentId(),
                    payment.payedAt(),
                    payment.amount(),
                    payment.reason(),
                    payment.isFuelPayment(),
                    payment.paymentStatus(),
                    user,
                    List.of(debtUserDTO)
            );
            when(readPaymentRepository.getAllPayments(page, 30)).thenReturn(new PagedResult<>(List.of(payment), true, 1));
            when(readDebtRepository.getDebtsByPaymentId(payment.paymentId())).thenReturn(List.of(debtUserDTO));

            PagedResult<List<PaymentUserDTO>> actual = readPaymentService.getAllPayments(page);

            assertTrue(actual.hasNext());
            assertEquals(1, actual.result().size());
            assertEquals(expected, actual.result().get(0));
        }
    }
}
