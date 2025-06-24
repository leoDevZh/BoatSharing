package org.example.backend.domain.payment;

import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.DebtRepository;
import org.example.backend.domain.payment.spi.PaymentRepository;
import org.example.backend.domain.payment.spi.ReadDebtRepository;
import org.example.backend.domain.payment.spi.ReadPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.example.backend.domain.payment.model.DebtStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private ReadDebtRepository readDebtRepository;

    @Mock
    ReadPaymentRepository readPaymentRepository;

    @InjectMocks
    private WritePaymentServiceImpl writePaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class CreatePaymentTests {
        @Test
        void shouldCreateNonFuelPaymentSuccessfully() {
            PaymentId paymentId = new PaymentId(1L);
            UserId user1 = new UserId(1L);
            UserId user2 = new UserId(2L);
            UserId user3 = new UserId(3L);
            double amount1 = 15;
            double amount2 = 20;
            double amount3 = 25;
            String reason = "Some reason";
            Boolean isFuelPayment = false;
            CreateDebt createDebt1 = new CreateDebt(amount2, user2);
            CreateDebt createDebt2 = new CreateDebt(amount3, user3);
            CreatePayment createPayment = new CreatePayment(amount1, reason, isFuelPayment, user1, List.of(createDebt1, createDebt2));
            when(paymentRepository.savePayment(any())).thenReturn(paymentId);
            when(userRepository.existsById(Set.of(user1, user2, user3))).thenReturn(true);
            ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);
            ArgumentCaptor<List<Debt>> debtsArgumentCaptor = ArgumentCaptor.forClass(List.class);

            assertDoesNotThrow(() -> writePaymentService.createPayment(createPayment));

            verify(userRepository, times(1)).existsById(Set.of(user1, user2, user3));
            verify(paymentRepository, times(1)).savePayment(paymentArgumentCaptor.capture());
            verify(debtRepository, times(1)).saveDebts(debtsArgumentCaptor.capture());
            Payment paymentCaptured = paymentArgumentCaptor.getValue();
            assertNull(paymentCaptured.getPaymentId());
            assertEquals(amount1, paymentCaptured.getAmount());
            assertEquals(reason, paymentCaptured.getReason());
            assertEquals(isFuelPayment, paymentCaptured.isFuelPayment());
            assertEquals(user1, paymentCaptured.getUserId());
            assertEquals(PaymentStatus.CHARGED, paymentCaptured.getStatus());
            List<Debt> debtsCaptured = debtsArgumentCaptor.getValue();
            assertEquals(amount2, debtsCaptured.get(0).getAmount());
            assertEquals(amount3, debtsCaptured.get(1).getAmount());
            assertEquals(user2, debtsCaptured.get(0).getUserId());
            assertEquals(user3, debtsCaptured.get(1).getUserId());
            assertEquals(OPEN, debtsCaptured.get(0).getStatus());
            assertEquals(OPEN, debtsCaptured.get(1).getStatus());
        }

        @Test
        void shouldCreateFuelPaymentSuccessfully() {
            UserId userId = new UserId(1L);
            double amount = 15;
            String reason = "Some reason";
            Boolean isFuelPayment = true;
            CreatePayment createPayment = new CreatePayment(amount, reason, isFuelPayment, userId, Collections.emptyList());
            when(userRepository.existsById(Set.of(userId))).thenReturn(true);
            ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);

            assertDoesNotThrow(() -> writePaymentService.createPayment(createPayment));

            verify(userRepository, times(1)).existsById(Set.of(userId));
            verify(paymentRepository, times(1)).savePayment(paymentArgumentCaptor.capture());
            verify(debtRepository, never()).saveDebts(any());
            Payment paymentCaptured = paymentArgumentCaptor.getValue();
            assertNull(paymentCaptured.getPaymentId());
            assertEquals(amount, paymentCaptured.getAmount());
            assertEquals(reason, paymentCaptured.getReason());
            assertEquals(isFuelPayment, paymentCaptured.isFuelPayment());
            assertEquals(userId, paymentCaptured.getUserId());
            assertEquals(PaymentStatus.OPEN, paymentCaptured.getStatus());
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {
            UserId userId = new UserId(1L);
            double amount = 15;
            String reason = "Some reason";
            Boolean isFuelPayment = true;
            CreatePayment createPayment = new CreatePayment(amount, reason, isFuelPayment, userId, Collections.emptyList());
            when(userRepository.existsById(Set.of(userId))).thenReturn(false);

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.createPayment(createPayment));

            assertEquals("Provided User not valid", exception.getMessage());
            verify(userRepository, times(1)).existsById(Set.of(userId));
            verify(paymentRepository, never()).savePayment(any());
            verify(debtRepository, never()).saveDebts(any());
        }

        @ParameterizedTest
        @MethodSource("invalidUserIdProvider")
        void shouldThrowExceptionWhenUserAppearsMultipleTimes(CreatePayment createPayment) {
            when(userRepository.existsById(anySet())).thenReturn(true);

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.createPayment(createPayment));

            assertEquals("Provided User not valid", exception.getMessage());
            verify(userRepository, never()).existsById(anySet());
            verify(paymentRepository, never()).savePayment(any());
            verify(debtRepository, never()).saveDebts(any());
        }

        @ParameterizedTest
        @MethodSource("invalidDataProvider")
        void shouldThrowExceptionWhenInvalidData(CreatePayment invalidCreatePayment) {
            when(userRepository.existsById(any())).thenReturn(true);

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.createPayment(invalidCreatePayment));

            assertEquals("Invalid Payment values provided", exception.getMessage());
            verify(userRepository, never()).existsById(any());
            verify(paymentRepository, never()).savePayment(any());
            verify(debtRepository, never()).saveDebts(any());
        }

        private static Stream<Arguments> invalidDataProvider() {
            return Stream.of(
                    Arguments.of(
                            new CreatePayment(-15, "Some reason", true, new UserId(1L), Collections.emptyList())
                    ),
                    Arguments.of(
                            new CreatePayment(15, "", true, new UserId(1L), Collections.emptyList())
                    ),
                    Arguments.of(
                            new CreatePayment(15, null, true, new UserId(1L), Collections.emptyList())
                    ),
                    Arguments.of(
                            new CreatePayment(15, "Some reason", true, null, Collections.emptyList())
                    )
            );
        }

        private static Stream<Arguments> invalidUserIdProvider() {
            UserId userId1 = new UserId(1L);
            UserId userId2 = new UserId(1L);
            double amount = 15;
            String reason = "Some reason";
            Boolean isFuelPayment = true;
            CreateDebt createDebt = new CreateDebt(amount, userId2);
            return Stream.of(
                    Arguments.of(
                            new CreatePayment(amount, reason, isFuelPayment, userId1, List.of(createDebt, createDebt))
                    ),
                    Arguments.of(
                            new CreatePayment(amount, reason, isFuelPayment, userId1, List.of(new CreateDebt(amount, userId1)))
                    ),
                    Arguments.of(
                            new CreatePayment(amount, reason, isFuelPayment, userId2, List.of(createDebt))
                    )
            );
        }
    }

    @Nested
    class SetDebtToPayedTests {
        @Test
        void shouldSetDebtToPayedSuccessfully() {
            DebtId debtId = new DebtId(1L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId = new UserId(3L);
            Debt debt = Debt.builder()
                    .debtId(debtId)
                    .amount(amount)
                    .status(OPEN)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.of(debt));
            Debt expectedUpdatedDebt = Debt.builder()
                    .debtId(debtId)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            ArgumentCaptor<List<Debt>> debtsArgumentCaptor = ArgumentCaptor.forClass(List.class);

            writePaymentService.setDebtToPayed(userId, debtId);

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, times(1)).saveDebts(debtsArgumentCaptor.capture());
            Debt actualDebt = debtsArgumentCaptor.getValue().get(0);
            assertEquals(actualDebt.getAmount(), expectedUpdatedDebt.getAmount());
            assertEquals(actualDebt.getStatus(), expectedUpdatedDebt.getStatus());
            assertEquals(actualDebt.getPaymentId(), expectedUpdatedDebt.getPaymentId());
            assertEquals(actualDebt.getUserId(), expectedUpdatedDebt.getUserId());
            assertEquals(actualDebt.getDebtId(), expectedUpdatedDebt.getDebtId());
        }

        @Test
        void shouldThrowExceptionWhenDebtNotFound() {
            DebtId debtId = new DebtId(1L);
            UserId userId = new UserId(3L);
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToPayed(userId, debtId));

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, never()).saveDebts(any());
            assertEquals("Debt not found", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenUserNotOwner() {
            DebtId debtId = new DebtId(1L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId = new UserId(3L);
            UserId loggedInUser = new UserId(4L);
            Debt debt = Debt.builder()
                    .debtId(debtId)
                    .amount(amount)
                    .status(OPEN)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.of(debt));

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToPayed(loggedInUser, debtId));

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, never()).saveDebts(any());
            assertEquals("Debt does not belong to User", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenDebtInitiallyNotOpen() {
            DebtId debtId = new DebtId(1L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId = new UserId(3L);
            Debt debt = Debt.builder()
                    .debtId(debtId)
                    .amount(amount)
                    .status(CLOSED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.of(debt));

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToPayed(userId, debtId));

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, never()).saveDebts(any());
            assertEquals("Debt is in wrong status", exception.getMessage());
        }
    }

    @Nested
    class SetDebtToClosedTests {
        @Test
        void shouldSetDebtToClosedOnOpenDebtsSuccessfully() {
            DebtId debtId1 = new DebtId(1L);
            DebtId debtId2 = new DebtId(2L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId = new UserId(3L);
            Debt debt1 = Debt.builder()
                    .debtId(debtId1)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            Debt debt2 = Debt.builder()
                    .debtId(debtId2)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .payedAt(LocalDateTime.now())
                    .fuelPayment(false)
                    .reason("Reason")
                    .amount(amount)
                    .status(PaymentStatus.CHARGED)
                    .userId(userId)
                    .build();
            DebtUserDTO debtUserDTO1 = new DebtUserDTO(debt1.getDebtId(), amount, debt1.getStatus(), new UserDTO(userId, "bla"));
            DebtUserDTO debtUserDTO2 = new DebtUserDTO(debt2.getDebtId(), amount, debt2.getStatus(), new UserDTO(userId, "bla"));
            when(readDebtRepository.getDebtById(debtId1)).thenReturn(Optional.of(debt1));
            when(readPaymentRepository.getPaymentById(paymentId)).thenReturn(Optional.of(payment));
            when(readDebtRepository.getDebtsByPaymentId(paymentId)).thenReturn(List.of(debtUserDTO1, debtUserDTO2));
            Debt expectedUpdatedDebt = Debt.builder()
                    .debtId(debtId1)
                    .amount(amount)
                    .status(CLOSED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            ArgumentCaptor<List<Debt>> debtsArgumentCaptor = ArgumentCaptor.forClass(List.class);

            writePaymentService.setDebtToClosed(userId, debtId1);

            verify(readDebtRepository, times(1)).getDebtById(debtId1);
            verify(readPaymentRepository, times(1)).getPaymentById(paymentId);
            verify(readDebtRepository, times(1)).getDebtsByPaymentId(paymentId);
            verify(debtRepository, times(1)).saveDebts(debtsArgumentCaptor.capture());
            Debt actualDebt = debtsArgumentCaptor.getValue().get(0);
            assertEquals(actualDebt.getAmount(), expectedUpdatedDebt.getAmount());
            assertEquals(actualDebt.getStatus(), expectedUpdatedDebt.getStatus());
            assertEquals(actualDebt.getPaymentId(), expectedUpdatedDebt.getPaymentId());
            assertEquals(actualDebt.getUserId(), expectedUpdatedDebt.getUserId());
            assertEquals(actualDebt.getDebtId(), expectedUpdatedDebt.getDebtId());
            verify(paymentRepository, never()).savePayment(any());
        }

        @Test
        void shouldSetDebtToClosedOnNoOpenDebtsSuccessfully() {
            DebtId debtId1 = new DebtId(1L);
            DebtId debtId2 = new DebtId(2L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId = new UserId(3L);
            Debt debt1 = Debt.builder()
                    .debtId(debtId1)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            Debt debt2 = Debt.builder()
                    .debtId(debtId2)
                    .amount(amount)
                    .status(CLOSED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .payedAt(LocalDateTime.now())
                    .fuelPayment(false)
                    .reason("Reason")
                    .amount(amount)
                    .status(PaymentStatus.CHARGED)
                    .userId(userId)
                    .build();
            DebtUserDTO debtUserDTO1 = new DebtUserDTO(debt1.getDebtId(), amount, debt1.getStatus(), new UserDTO(userId, "bla"));
            DebtUserDTO debtUserDTO2 = new DebtUserDTO(debt2.getDebtId(), amount, debt2.getStatus(), new UserDTO(userId, "bla"));
            when(readDebtRepository.getDebtById(debtId1)).thenReturn(Optional.of(debt1));
            when(readPaymentRepository.getPaymentById(paymentId)).thenReturn(Optional.of(payment));
            when(readDebtRepository.getDebtsByPaymentId(paymentId)).thenReturn(List.of(debtUserDTO1, debtUserDTO2));
            Debt expectedUpdatedDebt = Debt.builder()
                    .debtId(debtId1)
                    .amount(amount)
                    .status(CLOSED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            ArgumentCaptor<List<Debt>> debtsArgumentCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);

            writePaymentService.setDebtToClosed(userId, debtId1);

            verify(readDebtRepository, times(1)).getDebtById(debtId1);
            verify(readPaymentRepository, times(1)).getPaymentById(paymentId);
            verify(readDebtRepository, times(1)).getDebtsByPaymentId(paymentId);
            verify(debtRepository, times(1)).saveDebts(debtsArgumentCaptor.capture());
            Debt actualDebt = debtsArgumentCaptor.getValue().get(0);
            assertEquals(actualDebt.getAmount(), expectedUpdatedDebt.getAmount());
            assertEquals(actualDebt.getStatus(), expectedUpdatedDebt.getStatus());
            assertEquals(actualDebt.getPaymentId(), expectedUpdatedDebt.getPaymentId());
            assertEquals(actualDebt.getUserId(), expectedUpdatedDebt.getUserId());
            assertEquals(actualDebt.getDebtId(), expectedUpdatedDebt.getDebtId());
            verify(paymentRepository, times(1)).savePayment(paymentArgumentCaptor.capture());
            Payment actualPayment = paymentArgumentCaptor.getValue();
            assertEquals(PaymentStatus.CLOSED, actualPayment.getStatus());
        }

        @Test
        void shouldThrowExceptionWhenDebtNotFound() {
            DebtId debtId = new DebtId(1L);
            UserId userId = new UserId(3L);
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToClosed(userId, debtId));

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, never()).saveDebts(any());
            verify(paymentRepository, never()).savePayment(any());
            assertEquals("Debt not found", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenPaymentNotFound() {
            DebtId debtId = new DebtId(1L);
            UserId userId = new UserId(3L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            Debt debt1 = Debt.builder()
                    .debtId(debtId)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId)
                    .build();
            when(readDebtRepository.getDebtById(debtId)).thenReturn(Optional.of(debt1));
            when(readPaymentRepository.getPaymentById(paymentId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToClosed(userId, debtId));

            verify(readDebtRepository, times(1)).getDebtById(debtId);
            verify(debtRepository, never()).saveDebts(any());
            verify(paymentRepository, never()).savePayment(any());
            assertEquals("Payment not found", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenUserNotOwner() {
            DebtId debtId1 = new DebtId(1L);
            Double amount = 10.;
            PaymentId paymentId = new PaymentId(2L);
            UserId userId1 = new UserId(3L);
            UserId userId2 = new UserId(4L);
            Debt debt1 = Debt.builder()
                    .debtId(debtId1)
                    .amount(amount)
                    .status(PAYED)
                    .paymentId(paymentId)
                    .userId(userId1)
                    .build();
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .payedAt(LocalDateTime.now())
                    .fuelPayment(false)
                    .reason("Reason")
                    .amount(amount)
                    .status(PaymentStatus.CHARGED)
                    .userId(userId2)
                    .build();
            DebtUserDTO debtUserDTO1 = new DebtUserDTO(debt1.getDebtId(), amount, debt1.getStatus(), new UserDTO(userId1, "bla"));
            when(readDebtRepository.getDebtById(debtId1)).thenReturn(Optional.of(debt1));
            when(readPaymentRepository.getPaymentById(paymentId)).thenReturn(Optional.of(payment));
            when(readDebtRepository.getDebtsByPaymentId(paymentId)).thenReturn(List.of(debtUserDTO1));

            Exception exception = assertThrows(InvalidPaymentException.class, () -> writePaymentService.setDebtToClosed(userId1, debtId1));

            verify(readDebtRepository, times(1)).getDebtById(debtId1);
            verify(debtRepository, never()).saveDebts(any());
            verify(paymentRepository, never()).savePayment(any());
            assertEquals("Payment does not belong to User", exception.getMessage());
        }
    }
}
