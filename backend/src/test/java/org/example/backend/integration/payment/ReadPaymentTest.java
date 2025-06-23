package org.example.backend.integration.payment;

import org.example.backend.TestDBConfiguration;
import org.example.backend.domain.payment.model.DebtStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.example.backend.domain.payment.model.PaymentStatus.OPEN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDBConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
public class ReadPaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaPaymentRepository paymentRepository;

    @Autowired
    private JpaDebtRepository debtRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User user1;
    private User user2;
    private CustomUserDetail customUserDetail;
    private Payment payment1;
    private Payment payment2;
    private Debt debt1;
    private Debt debt2;

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
        payment1 = Payment.builder()
                .paymentDate(LocalDateTime.now().plusDays(1).truncatedTo(SECONDS))
                .amount(10.)
                .status(OPEN)
                .reason("reason1")
                .isFuelPayment(false)
                .userId(user1.getId())
                .build();
        paymentRepository.save(payment1);
        payment2 = Payment.builder()
                .paymentDate(LocalDateTime.now().truncatedTo(SECONDS))
                .amount(20.)
                .status(OPEN)
                .reason("reason1")
                .isFuelPayment(false)
                .userId(user2.getId())
                .build();
        paymentRepository.save(payment2);
        debt1 = Debt.builder()
                .amount(11.)
                .status(DebtStatus.OPEN)
                .paymentId(payment1.getId())
                .userId(user2.getId())
                .build();
        debtRepository.save(debt1);
        debt2 = Debt.builder()
                .amount(21.)
                .status(DebtStatus.OPEN)
                .paymentId(payment2.getId())
                .userId(user1.getId())
                .build();
        debtRepository.save(debt2);
    }

    @Nested
    class GetPaymentsFromLoggedInUser {
        @Test
        void getPaymentsFromLoggedInUserOnSuccess() throws Exception {
            mockMvc.perform(get("/api/read-payment/all-from-logged-user")
                            .param("page", "0")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(1))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalNumber").value(1))
                    .andExpect(jsonPath("$.result[0].paymentId.value").value(payment1.getId()))
                    .andExpect(jsonPath("$.result[0].payedAt").value(payment1.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[0].amount").value(payment1.getAmount()))
                    .andExpect(jsonPath("$.result[0].reason").value(payment1.getReason()))
                    .andExpect(jsonPath("$.result[0].isFuelPayment").value(payment1.getIsFuelPayment()))
                    .andExpect(jsonPath("$.result[0].paymentStatus").value(payment1.getStatus().toString()))
                    .andExpect(jsonPath("$.result[0].creditor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[0].creditor.username").value(user1.getUsername()))
                    .andExpect(jsonPath("$.result[0].debitors.length()").value(1))
                    .andExpect(jsonPath("$.result[0].debitors[0].debtId.value").value(debt1.getId()))
                    .andExpect(jsonPath("$.result[0].debitors[0].amount").value(debt1.getAmount()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debtStatus").value(debt1.getStatus().toString()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debitor.userId.value").value(debt1.getUserId()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debitor.username").value(user2.getUsername()));
        }

        @Test
        void getPaymentsFromLoggedInUserOnPageToBig() throws Exception {
            mockMvc.perform(get("/api/read-payment/all-from-logged-user")
                            .param("page", "1")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }

        @Test
        void getPaymentsFromLoggedInUserOnPageInvalid() throws Exception {
            mockMvc.perform(get("/api/read-payment/all-from-logged-user")
                            .param("page", "a")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
        }

        @Test
        void getPaymentsFromLoggedInUserOnPageMissing() throws Exception {
            mockMvc.perform(get("/api/read-payment/all-from-logged-user")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getPaymentsFromLoggedInUserNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/read-payment/all-from-logged-user")
                            .param("page", "1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetAllPayments {
        @Test
        void getAllPaymentsOnSuccess() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .param("page", "0")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalNumber").value(2))
                    .andExpect(jsonPath("$.result[0].paymentId.value").value(payment1.getId()))
                    .andExpect(jsonPath("$.result[0].payedAt").value(payment1.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[0].amount").value(payment1.getAmount()))
                    .andExpect(jsonPath("$.result[0].reason").value(payment1.getReason()))
                    .andExpect(jsonPath("$.result[0].isFuelPayment").value(payment1.getIsFuelPayment()))
                    .andExpect(jsonPath("$.result[0].paymentStatus").value(payment1.getStatus().toString()))
                    .andExpect(jsonPath("$.result[0].creditor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[0].creditor.username").value(user1.getUsername()))
                    .andExpect(jsonPath("$.result[0].debitors.length()").value(1))
                    .andExpect(jsonPath("$.result[0].debitors[0].debtId.value").value(debt1.getId()))
                    .andExpect(jsonPath("$.result[0].debitors[0].amount").value(debt1.getAmount()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debtStatus").value(debt1.getStatus().toString()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debitor.userId.value").value(debt1.getUserId()))
                    .andExpect(jsonPath("$.result[0].debitors[0].debitor.username").value(user2.getUsername()))
                    .andExpect(jsonPath("$.result[1].paymentId.value").value(payment2.getId()))
                    .andExpect(jsonPath("$.result[1].payedAt").value(payment2.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[1].amount").value(payment2.getAmount()))
                    .andExpect(jsonPath("$.result[1].reason").value(payment2.getReason()))
                    .andExpect(jsonPath("$.result[1].isFuelPayment").value(payment2.getIsFuelPayment()))
                    .andExpect(jsonPath("$.result[1].paymentStatus").value(payment2.getStatus().toString()))
                    .andExpect(jsonPath("$.result[1].creditor.userId.value").value(user2.getId()))
                    .andExpect(jsonPath("$.result[1].creditor.username").value(user2.getUsername()))
                    .andExpect(jsonPath("$.result[1].debitors.length()").value(1))
                    .andExpect(jsonPath("$.result[1].debitors[0].debtId.value").value(debt2.getId()))
                    .andExpect(jsonPath("$.result[1].debitors[0].amount").value(debt2.getAmount()))
                    .andExpect(jsonPath("$.result[1].debitors[0].debtStatus").value(debt2.getStatus().toString()))
                    .andExpect(jsonPath("$.result[1].debitors[0].debitor.userId.value").value(debt2.getUserId()))
                    .andExpect(jsonPath("$.result[1].debitors[0].debitor.username").value(user1.getUsername()));
        }

        @Test
        void getAllPaymentsOnPageToBig() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .param("page", "1")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }

        @Test
        void getAllPaymentsFromLoggedInUserOnPageInvalid() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .param("page", "a")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
        }

        @Test
        void getAllPaymentsOnPageMissing() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getAllPaymentsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .param("page", "1"))
                    .andExpect(status().isForbidden());
        }
    }
}
