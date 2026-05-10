package org.example.backend.integration.payment;

import org.example.backend.TestDBConfiguration;
import org.example.backend.domain.payment.model.DebtStatus;
import org.example.backend.infrastructure.repository.boat.Boat;
import org.example.backend.infrastructure.repository.boat.JpaBoatRepository;
import org.example.backend.infrastructure.repository.debt.Debt;
import org.example.backend.infrastructure.repository.debt.JpaDebtRepository;
import org.example.backend.infrastructure.repository.invoice.Invoice;
import org.example.backend.infrastructure.repository.invoice.JpaInvoiceRepository;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

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

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaInvoiceRepository invoiceRepository;

    private User user1;
    private User user2;
    private User harry;
    private CustomUserDetail customUserDetail;
    private CustomUserDetail harryUserDetail;
    private Payment payment1;
    private Payment payment2;
    private Debt debt1;
    private Debt debt2;
    private Invoice invoice1;
    private Invoice invoice2;
    private Boat boat1;

    @BeforeEach
    void setup() {
        debtRepository.deleteAll();
        paymentRepository.deleteAll();
        userRepository.deleteAll();
        invoiceRepository.deleteAll();
        boatRepository.deleteAll();
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
        harry = User.builder()
                .username("Harry")
                .password("pwd")
                .build();
        userRepository.save(harry);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
        harryUserDetail = CustomUserDetail.builder()
                .id(harry.getId())
                .username(harry.getUsername())
                .password(harry.getPassword())
                .build();
        payment1 = Payment.builder()
                .paymentDate(LocalDateTime.now().plusDays(1).truncatedTo(SECONDS))
                .amount(10.)
                .status(OPEN)
                .reason("reason1")
                .isFuelPayment(true)
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
        boat1 = Boat.builder()
                .name("Boat")
                .userIds(Set.of(user1.getId(), harry.getId()))
                .build();
        boatRepository.save(boat1);
        invoice1 = Invoice.builder()
                .startDate(LocalDateTime.of(2025, 4, 14, 0, 0))
                .endDate(LocalDateTime.of(2025, 4, 18, 23, 59))
                .boatId(boat1.getId())
                .build();
        invoiceRepository.save(invoice1);
        invoice2 = Invoice.builder()
                .startDate(LocalDateTime.of(2025, 4, 19, 0, 0))
                .endDate(LocalDateTime.of(2025, 4, 22, 23, 59))
                .boatId(boat1.getId())
                .build();
        invoiceRepository.save(invoice2);
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
        void getAllPaymentsOnPageInvalid() throws Exception {
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

        @Test
        void getAllPaymentsOnUserIsHarry() throws Exception {
            mockMvc.perform(get("/api/read-payment/all")
                            .param("page", "0")
                            .with(user(harryUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(1))
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
                    .andExpect(jsonPath("$.result[0].debitors[0].debitor.username").value(user2.getUsername()));        }
    }

    @Nested
    class GetAllOpenDebtsFromLoggedInUser {
        @Test
        void getAllOpenDebtsOnSuccess() throws Exception {
            Debt debt3 = Debt.builder()
                    .amount(21.)
                    .status(DebtStatus.CLOSED)
                    .paymentId(payment2.getId())
                    .userId(user1.getId())
                    .build();
            Debt debt4 = Debt.builder()
                    .amount(21.)
                    .status(DebtStatus.OPEN)
                    .paymentId(payment1.getId())
                    .userId(user1.getId())
                    .build();
            debtRepository.save(debt3);
            debtRepository.save(debt4);

            mockMvc.perform(get("/api/read-payment/open-debts")
                            .param("page", "0")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalNumber").value(2))
                    .andExpect(jsonPath("$.result[1].paymentId.value").value(payment2.getId()))
                    .andExpect(jsonPath("$.result[1].payedAt").value(payment2.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[1].reason").value(payment2.getReason()))
                    .andExpect(jsonPath("$.result[1].creditor.userId.value").value(user2.getId()))
                    .andExpect(jsonPath("$.result[1].creditor.username").value(user2.getUsername()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debtId.value").value(debt2.getId()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.amount").value(debt2.getAmount()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debtStatus").value(DebtStatus.OPEN.toString()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debitor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debitor.username").value(user1.getUsername()))

                    .andExpect(jsonPath("$.result[0].paymentId.value").value(payment1.getId()))
                    .andExpect(jsonPath("$.result[0].payedAt").value(payment1.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[0].reason").value(payment1.getReason()))
                    .andExpect(jsonPath("$.result[0].creditor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[0].creditor.username").value(user1.getUsername()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debtId.value").value(debt4.getId()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.amount").value(debt4.getAmount()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debtStatus").value(DebtStatus.OPEN.toString()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debitor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debitor.username").value(user1.getUsername()));
        }

        @Test
        void getAllOpenDebtsOnPageToBig() throws Exception {
            mockMvc.perform(get("/api/read-payment/open-debts")
                            .param("page", "1")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }

        @Test
        void getAllOpenDebtsOnPageInvalid() throws Exception {
            mockMvc.perform(get("/api/read-payment/open-debts")
                            .param("page", "a")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
        }

        @Test
        void getAllOpenDebtsOnPageMissing() throws Exception {
            mockMvc.perform(get("/api/read-payment/open-debts")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getAllPaymentsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/read-payment/open-debts")
                            .param("page", "1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetAllDebtsToCheck {
        @Test
        void getAllDebtsToCheckOnSuccess() throws Exception {
            Payment payment3 = Payment.builder()
                    .paymentDate(LocalDateTime.now().truncatedTo(SECONDS))
                    .amount(10.)
                    .status(OPEN)
                    .reason("reason3")
                    .isFuelPayment(false)
                    .userId(user1.getId())
                    .build();
            paymentRepository.save(payment3);
            Debt debt3 = Debt.builder()
                    .amount(31.)
                    .status(DebtStatus.PAYED)
                    .paymentId(payment2.getId())
                    .userId(user2.getId())
                    .build();
            Debt debt4 = Debt.builder()
                    .amount(41.)
                    .status(DebtStatus.PAYED)
                    .paymentId(payment1.getId())
                    .userId(user2.getId())
                    .build();
            Debt debt5 = Debt.builder()
                    .amount(51.)
                    .status(DebtStatus.PAYED)
                    .paymentId(payment3.getId())
                    .userId(user2.getId())
                    .build();
            debtRepository.save(debt3);
            debtRepository.save(debt4);
            debtRepository.save(debt5);

            mockMvc.perform(get("/api/read-payment/debts-to-check")
                            .param("page", "0")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalNumber").value(2))
                    .andExpect(jsonPath("$.result[0].paymentId.value").value(payment1.getId()))
                    .andExpect(jsonPath("$.result[0].payedAt").value(payment1.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[0].reason").value(payment1.getReason()))
                    .andExpect(jsonPath("$.result[0].creditor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[0].creditor.username").value(user1.getUsername()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debtId.value").value(debt4.getId()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.amount").value(debt4.getAmount()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debtStatus").value(DebtStatus.PAYED.toString()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debitor.userId.value").value(user2.getId()))
                    .andExpect(jsonPath("$.result[0].debtUserDTO.debitor.username").value(user2.getUsername()))

                    .andExpect(jsonPath("$.result[1].paymentId.value").value(payment3.getId()))
                    .andExpect(jsonPath("$.result[1].payedAt").value(payment3.getPaymentDate().toString()))
                    .andExpect(jsonPath("$.result[1].reason").value(payment3.getReason()))
                    .andExpect(jsonPath("$.result[1].creditor.userId.value").value(user1.getId()))
                    .andExpect(jsonPath("$.result[1].creditor.username").value(user1.getUsername()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debtId.value").value(debt5.getId()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.amount").value(debt5.getAmount()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debtStatus").value(DebtStatus.PAYED.toString()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debitor.userId.value").value(user2.getId()))
                    .andExpect(jsonPath("$.result[1].debtUserDTO.debitor.username").value(user2.getUsername()));
        }

        @Test
        void getAllDebtsToCheckOnPageToBig() throws Exception {
            mockMvc.perform(get("/api/read-payment/debts-to-check")
                            .param("page", "1")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }

        @Test
        void getAllDebtsToCheckOnPageInvalid() throws Exception {
            mockMvc.perform(get("/api/read-payment/debts-to-check")
                            .param("page", "a")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
        }

        @Test
        void getAllDebtsToCheckOnPageMissing() throws Exception {
            mockMvc.perform(get("/api/read-payment/debts-to-check")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getAllDebtsToCheckNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/read-payment/debts-to-check")
                            .param("page", "1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetNextFuelPeriod {
        @Test
        void shouldGetNextFuelPaymentPeriodOnExisting() throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String expectedStartDate = invoice2.getEndDate()
                    .plusDays(1)
                    .with(LocalTime.MIN)
                    .format(formatter);
            String expectedEndDate = LocalDateTime.now()
                    .minusDays(1)
                    .with(LocalTime.MAX)
                    .format(formatter);

            mockMvc.perform(get("/api/read-payment/next-invoice-period")
                            .param("boatId", boat1.getId().toString())
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startDate").value(expectedStartDate))
                    .andExpect(jsonPath("$.endDate").value(expectedEndDate));
        }

        @Test
        void shouldGetNextFuelPaymentPeriodOnNonExisting() throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String expectedStartDate = LocalDateTime.of(2025, 1, 1, 0, 0)
                    .format(formatter);
            String expectedEndDate = LocalDateTime.now()
                    .minusDays(1)
                    .with(LocalTime.MAX)
                    .format(formatter);
            invoiceRepository.deleteAll();

            mockMvc.perform(get("/api/read-payment/next-invoice-period")
                            .param("boatId", boat1.getId().toString())
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startDate").value(expectedStartDate))
                    .andExpect(jsonPath("$.endDate").value(expectedEndDate));
        }

        @Test
        void shouldGetNextFuelPaymentOnBoatIdMissing() throws Exception {
            mockMvc.perform(get("/api/read-payment/next-invoice-period")
                            .with(user(customUserDetail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldGetNextFuelPaymentPeriodOnNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/read-payment/next-invoice-period")
                            .param("boatId", boat1.getId().toString()))
                    .andExpect(status().isForbidden());
        }
    }
}
