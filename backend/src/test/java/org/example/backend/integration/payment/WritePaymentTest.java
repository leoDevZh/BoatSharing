package org.example.backend.integration.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.TestDBConfiguration;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.payment.model.DebtStatus;
import org.example.backend.domain.payment.model.PaymentStatus;
import org.example.backend.infrastructure.controller.payment.model.CreateDebtDTO;
import org.example.backend.infrastructure.controller.payment.model.CreatePaymentDTO;
import org.example.backend.infrastructure.repository.debt.Debt;
import org.example.backend.infrastructure.repository.debt.JpaDebtRepository;
import org.example.backend.infrastructure.repository.payment.JpaPaymentRepository;
import org.example.backend.infrastructure.repository.payment.Payment;
import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.repository.user.User;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.example.backend.domain.payment.model.DebtStatus.PAYED;
import static org.example.backend.domain.payment.model.PaymentStatus.OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDBConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
public class WritePaymentTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaPaymentRepository paymentRepository;

    @Autowired
    private JpaDebtRepository debtRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;
    private CustomUserDetail customUserDetail;

    @BeforeEach
    void setup() {
        debtRepository.deleteAll();
        paymentRepository.deleteAll();
        userRepository.deleteAll();
        user1 = User.builder()
                .username("Captain")
                .password("pwd")
                .build();
        userRepository.save(user1);
        user2 = User.builder()
                .username("Cadet")
                .password("pwd")
                .build();
        userRepository.save(user2);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
    }

    @Nested
    class CreatePaymentTests {
        @Test
        void createNonFuelPaymentOnSuccess() throws Exception {
            double amount1 = 150.15;
            double amount2 = 75.5;
            String reason = "Some reason";
            boolean isFuelPayment = false;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount1,
                    reason,
                    isFuelPayment,
                    List.of(new CreateDebtDTO(amount2, new UserId(user2.getId())))
            );
            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isCreated());
            List<Payment> payments = paymentRepository.findAll();
            assertEquals(1, payments.size());
            Payment payment = payments.get(0);
            assertEquals(amount1, payment.getAmount());
            assertEquals(reason, payment.getReason());
            assertEquals(PaymentStatus.CHARGED, payment.getStatus());
            assertEquals(isFuelPayment, payment.getIsFuelPayment());
            assertEquals(user1.getId(), payment.getUserId());
            List<Debt> debts = debtRepository.findAll();
            assertEquals(1, debts.size());
            Debt debt = debts.get(0);
            assertEquals(amount2, debt.getAmount());
            assertEquals(DebtStatus.OPEN, debt.getStatus());
            assertEquals(payment.getId(), debt.getPaymentId());
            assertEquals(user2.getId(), debt.getUserId());
        }

        @Test
        void createFuelPaymentOnSuccess() throws Exception {
            double amount1 = 150.15;
            String reason = "Some reason";
            boolean isFuelPayment = true;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount1,
                    reason,
                    isFuelPayment
            );
            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isCreated());
            List<Payment> payments = paymentRepository.findAll();
            assertEquals(1, payments.size());
            Payment payment = payments.get(0);
            assertEquals(amount1, payment.getAmount());
            assertEquals(reason, payment.getReason());
            assertEquals(PaymentStatus.OPEN, payment.getStatus());
            assertEquals(isFuelPayment, payment.getIsFuelPayment());
            assertEquals(user1.getId(), payment.getUserId());
            List<Debt> debts = debtRepository.findAll();
            assertEquals(0, debts.size());
        }

        @Test
        void createPaymentOnAmountLessThanZero() throws Exception {
            double amount1 = -1;
            String reason = "Some reason";
            boolean isFuelPayment = true;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount1,
                    reason,
                    isFuelPayment
            );
            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Payment values provided"));
        }

        @Test
        void createPaymentOnReasonIsEmpty() throws Exception {
            double amount1 = 1;
            String reason = "";
            boolean isFuelPayment = true;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount1,
                    reason,
                    isFuelPayment
            );
            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Payment values provided"));
        }

        @Test
        void createPaymentOnNotAuthenticated() throws Exception {
            double amount1 = 1;
            String reason = "Some reason";
            boolean isFuelPayment = true;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount1,
                    reason,
                    isFuelPayment
            );
            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @MethodSource("invalidRequestBodies")
        void createPaymentOnInvalidBody(String jsonRequest) throws Exception {
            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provide a valid Requestbody"));
        }

        @Test
        void createPaymentOnUserInvalidOne() throws Exception {
            double amount = 150.15;
            String reason = "Some reason";
            boolean isFuelPayment = false;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount,
                    reason,
                    isFuelPayment,
                    List.of(new CreateDebtDTO(amount, new UserId(user1.getId())))
            );

            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provided User not valid"));
        }

        @Test
        void createPaymentOnUserInvalidTwo() throws Exception {
            double amount = 150.15;
            String reason = "Some reason";
            boolean isFuelPayment = false;
            CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO(
                    amount,
                    reason,
                    isFuelPayment,
                    List.of(new CreateDebtDTO(amount, new UserId(user2.getId())),
                            new CreateDebtDTO(amount, new UserId(user2.getId())))
            );

            String paymentJson = objectMapper.writeValueAsString(createPaymentDTO);

            mockMvc.perform(post("/api/payment/create")
                            .with(user(customUserDetail))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provided User not valid"));
        }

        private static Stream<String> invalidRequestBodies() {
            return Stream.of(
                    "{\"reason\":\"Some reason\",\"debts\":[{\"amount\":75.5,\"userId\":{\"value\":2}}],\"fuelPayment\":false}",
                    "{\"amount\":\"2\",\"debts\":[{\"amount\":75.5,\"userId\":{\"value\":2}}],\"fuelPayment\":false}",
                    "{\"amount\":\"2\",\"reason\":\"Some reason\",\"debts\":[{\"amount\":75.5,\"userId\":{\"value\":2}}]}",
                    "{\"amount\":\"2\",\"reason\":\"Some reason\",\"debts\":[{\"userId\":{\"value\":2}}],\"fuelPayment\":false}",
                    "{\"amount\":\"2\",\"reason\":\"Some reason\",\"debts\":[{\"amount\":75.5}],\"fuelPayment\":false}",
                    "{}"
            );
        }
    }

    @Nested
    class SetDebtToPayedTest {
        Payment payment;
        Debt debt;
        Debt debt2;
        Debt debt3;

        @BeforeEach
        void setup() {
            payment = Payment.builder()
                    .paymentDate(LocalDateTime.now().plusDays(1).truncatedTo(SECONDS))
                    .amount(10.)
                    .status(OPEN)
                    .reason("reason1")
                    .isFuelPayment(false)
                    .userId(user2.getId())
                    .build();
            paymentRepository.save(payment);
            debt = Debt.builder()
                    .amount(11.)
                    .status(DebtStatus.OPEN)
                    .paymentId(payment.getId())
                    .userId(user1.getId())
                    .build();
            debt2 = Debt.builder()
                    .amount(11.)
                    .status(DebtStatus.OPEN)
                    .paymentId(payment.getId())
                    .userId(user2.getId())
                    .build();
            debt3 = Debt.builder()
                    .amount(11.)
                    .status(DebtStatus.CLOSED)
                    .paymentId(payment.getId())
                    .userId(user1.getId())
                    .build();
            debtRepository.save(debt);
            debtRepository.save(debt2);
            debtRepository.save(debt3);
        }

        @Test
        void updateDebtStateOnSuccess() throws Exception {
            mockMvc.perform(post("/api/payment/debt-to-payed/" + debt.getId())
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk());
            Debt actualDebt = debtRepository.findById(debt.getId()).get();
            assertEquals(PAYED, actualDebt.getStatus());
        }

        @Test
        void updateDebtStateOnDebtNotFound() throws Exception {
            mockMvc.perform(post("/api/payment/debt-to-payed/" + (debt.getId() + 999))
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Debt not found"));
        }

        @Test
        void updateDebtStateOnUserNotOwner() throws Exception {
            mockMvc.perform(post("/api/payment/debt-to-payed/" + debt2.getId())
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Debt does not belong to User"));
            Debt actualDebt = debtRepository.findById(debt2.getId()).get();
            assertEquals(DebtStatus.OPEN, actualDebt.getStatus());
        }

        @Test
        void updateDebtStateOnDebtInWronStatus() throws Exception {
            mockMvc.perform(post("/api/payment/debt-to-payed/" + debt3.getId())
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Debt is in wrong status"));
            Debt actualDebt = debtRepository.findById(debt3.getId()).get();
            assertEquals(DebtStatus.CLOSED, actualDebt.getStatus());
        }
    }

    @Nested
    class DeletePaymentTest {
        Payment payment;
        Payment payment2;
        Debt debt;

        @BeforeEach
        void setup() throws Exception {
            payment = Payment.builder()
                    .paymentDate(LocalDateTime.now().plusDays(1).truncatedTo(SECONDS))
                    .amount(10.)
                    .status(OPEN)
                    .reason("reason1")
                    .isFuelPayment(false)
                    .userId(user1.getId())
                    .build();
            payment2 = Payment.builder()
                    .paymentDate(LocalDateTime.now().plusDays(1).truncatedTo(SECONDS))
                    .amount(10.)
                    .status(OPEN)
                    .reason("reason1")
                    .isFuelPayment(false)
                    .userId(user2.getId())
                    .build();
            paymentRepository.save(payment);
            paymentRepository.save(payment2);
            debt = Debt.builder()
                    .amount(11.)
                    .status(DebtStatus.OPEN)
                    .paymentId(payment.getId())
                    .userId(user1.getId())
                    .build();
            debtRepository.save(debt);
        }

        @Test
        void deletePaymentOnSuccess() throws Exception {
            mockMvc.perform(delete("/api/payment/delete/" + payment.getId())
                            .with(user(customUserDetail)))
                    .andExpect(status().isNoContent());

            List<Payment> payments = paymentRepository.findAll();
            List<Debt> debts = debtRepository.findAll();
            assertEquals(1, payments.size());
            assertEquals(0, debts.size());
        }

        @Test
        void deletePaymentOnUserNotOwner() throws Exception {
            mockMvc.perform(delete("/api/payment/delete/" + payment2.getId())
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Payment does not belong to User"));

            List<Payment> payments = paymentRepository.findAll();
            List<Debt> debts = debtRepository.findAll();
            assertEquals(2, payments.size());
            assertEquals(1, debts.size());
        }

        @Test
        void deletePaymentOnNotAuthorized() throws Exception {
            mockMvc.perform(delete("/api/payment/delete/" + payment.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
