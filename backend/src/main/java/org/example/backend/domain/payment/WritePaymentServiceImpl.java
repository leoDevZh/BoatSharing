package org.example.backend.domain.payment;

import org.example.backend.domain.DomainService;
import org.example.backend.domain.User.model.UserDTO;
import org.example.backend.domain.User.model.UserId;
import org.example.backend.domain.User.spi.UserRepository;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.domain.payment.api.ReadPaymentService;
import org.example.backend.domain.payment.api.WritePaymentService;
import org.example.backend.domain.payment.model.*;
import org.example.backend.domain.payment.spi.*;
import org.example.backend.domain.reservation.api.ReadReservationService;
import org.example.backend.domain.reservation.model.ReservationUserDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@DomainService
public class WritePaymentServiceImpl implements WritePaymentService {

    private PaymentRepository paymentRepository;
    private DebtRepository debtRepository;
    private UserRepository userRepository;
    private ReadDebtRepository readDebtRepository;
    private ReadPaymentRepository readPaymentRepository;
    private ReadPaymentService readPaymentService;
    private ReadReservationService readReservationService;
    private WriteInvoiceRepository writeInvoiceRepository;

    public WritePaymentServiceImpl(PaymentRepository paymentRepository, DebtRepository debtRepository, UserRepository userRepository, ReadDebtRepository readDebtRepository, ReadPaymentRepository readPaymentRepository, ReadPaymentService readPaymentService, ReadReservationService readReservationService, WriteInvoiceRepository writeInvoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.readDebtRepository = readDebtRepository;
        this.readPaymentRepository = readPaymentRepository;
        this.readPaymentService = readPaymentService;
        this.readReservationService = readReservationService;
        this.writeInvoiceRepository = writeInvoiceRepository;
    }

    @Override
    public void createPayment(CreatePayment createPayment) {
        Payment payment = Payment.builder()
                .amount(createPayment.amount())
                .reason(createPayment.reason())
                .fuelPayment(createPayment.isFuelPayment())
                .status(createPayment.isFuelPayment() ? PaymentStatus.OPEN : PaymentStatus.CHARGED)
                .payedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .userId(createPayment.userId())
                .build();

        Set<UserId> userIds = new HashSet<>();
        userIds.add(createPayment.userId());
        createPayment.debts().forEach(debt -> {
            userIds.add(debt.userId());
        });
        if (!(userIds.size() == (1 + createPayment.debts().size()) && userRepository.existsById(userIds))) {
            throw new InvalidPaymentException("Provided User not valid");
        }

        PaymentId paymentId = paymentRepository.savePayment(payment);
        if (!createPayment.isFuelPayment()) {
            List<Debt> debts = createPayment.debts().stream()
                    .map(createDebt -> Debt.builder()
                            .amount(createDebt.amount())
                            .status(DebtStatus.OPEN)
                            .userId(createDebt.userId())
                            .paymentId(paymentId)
                            .build())
                    .toList();
            debtRepository.saveDebts(debts);
        }
    }

    @Override
    public void setDebtToPayed(UserId loggedInUser, DebtId debtId) {
        Debt debt = readDebtRepository.getDebtById(debtId).orElseThrow(() -> new InvalidPaymentException("Debt not found"));
        if (!debt.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Debt does not belong to User");
        }
        debt.setPayed();
        debtRepository.saveDebts(List.of(debt));
    }

    @Transactional
    @Override
    public void setDebtToClosed(UserId loggedInUser, DebtId debtId) {
        Debt debt = readDebtRepository.getDebtById(debtId).orElseThrow(() -> new InvalidPaymentException("Debt not found"));
        Payment payment = readPaymentRepository.getPaymentById(debt.getPaymentId()).orElseThrow(() -> new InvalidPaymentException("Payment not found"));
        List<DebtUserDTO> debts = readDebtRepository.getDebtsByPaymentId(payment.getPaymentId());
        if (!payment.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Payment does not belong to User");
        }
        debt.setClosed();
        debtRepository.saveDebts(List.of(debt));

        boolean updatePayment = true;
        for (DebtUserDTO debtUserDTO : debts) {
            if (debtUserDTO.debtId().equals(debtId)) {
                continue;
            }
            if (debtUserDTO.debtStatus() != DebtStatus.CLOSED) {
                updatePayment = false;
            }
        }
        if (updatePayment) {
            payment.setClosed();
            paymentRepository.savePayment(payment);
        }
    }

    @Override
    public void deletePayment(UserId loggedInUser, PaymentId paymentId) {
        Payment payment = readPaymentRepository.getPaymentById(paymentId).orElseThrow(() -> new InvalidPaymentException("Payment not found"));
        if (!payment.isOwner(loggedInUser)) {
            throw new InvalidPaymentException("Payment does not belong to User");
        }
        paymentRepository.deletePayment(paymentId);
    }

    @Override
    @Transactional
    public FuelInvoiceDTO createInvoice(BoatId boatId, UserId loggedInUser) {
        FuelPaymentPeriodDTO nextFuelPaymentPeriod = readPaymentService.getNextFuelPaymentPeriod(boatId);
        List<UserDTO> users = userRepository.getUsersByBoatId(boatId);

        List<ReservationUserDTO> reservationsForPeriod = readReservationService.getReservationForPeriod(nextFuelPaymentPeriod.startDate(), nextFuelPaymentPeriod.endDate(), boatId, loggedInUser);
        Map<UserId, Double> hoursPerUser = mapHoursToUsers(reservationsForPeriod);
        double totalHours = hoursPerUser.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        List<PaymentWithUsername> paymentsForPeriod = readPaymentRepository.getFuelPaymentsForPeriod(nextFuelPaymentPeriod.startDate(), nextFuelPaymentPeriod.endDate());
        Map<UserId, Double> payedAmountPerUsers = mapPayedAmountToUsers(paymentsForPeriod);
        double totalPayed = payedAmountPerUsers.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        paymentsForPeriod.forEach(payment -> {
            paymentRepository.setPaymentToClosed(payment.paymentId());
        });

        Map<UserId, Double> creditor = new HashMap<>();
        Map<UserId, Double> debitor = new HashMap<>();
        calculateOpenPayments(users, hoursPerUser, totalHours, totalPayed, payedAmountPerUsers, debitor, creditor);
        List<PaymentId> paymentsCharged = chargeOpenPayments(creditor, debitor);

        List<UserTotalHoursDTO> userTotalHoursDTOs = users.stream().map(userDTO -> {
                    Double hours = hoursPerUser.getOrDefault(userDTO.userId(), 0.0);
                    return new UserTotalHoursDTO(hours, userDTO);
                })
                .toList();
        List<UserTotalPayedDTO> userTotalPayedDTOs = users.stream().map(userDTO -> {
                    Double hours = payedAmountPerUsers.getOrDefault(userDTO.userId(), 0.0);
                    return new UserTotalPayedDTO(hours, userDTO);
                })
                .toList();
        this.writeInvoiceRepository.createInvoice(nextFuelPaymentPeriod.startDate(), nextFuelPaymentPeriod.endDate(), boatId);
        return new FuelInvoiceDTO(nextFuelPaymentPeriod, totalHours, totalPayed, userTotalHoursDTOs, userTotalPayedDTOs, paymentsCharged);
    }

    private List<PaymentId> chargeOpenPayments(Map<UserId, Double> creditor, Map<UserId, Double> debitor) {
        List<PaymentId> paymentsCharged = new ArrayList<>();
        List<Map.Entry<UserId, Double>> sortedCreditor = new ArrayList<>(creditor.entrySet());
        sortedCreditor.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<Map.Entry<UserId, Double>> sortedDebitor = new ArrayList<>(debitor.entrySet());
        sortedDebitor.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int c = 0;
        int d = 0;
        double currentPaymentDebited = 0.;
        List<Debt.DebtBuilder> currentDebtBuilders = new ArrayList<>();
        while (c < sortedCreditor.size() && d < sortedDebitor.size()) {
            double openCreditorAmount = sortedCreditor.get(c).getValue();
            double openDebitorAmount = sortedDebitor.get(d).getValue();
            if (openCreditorAmount > openDebitorAmount) {
                currentPaymentDebited += openDebitorAmount;
                sortedCreditor.get(c).setValue(openCreditorAmount - openDebitorAmount);
                Debt.DebtBuilder debtToAdd = Debt.builder()
                        .amount(openDebitorAmount)
                        .userId(sortedDebitor.get(d).getKey())
                        .status(DebtStatus.OPEN);
                currentDebtBuilders.add(debtToAdd);
                d++;
                if ((openCreditorAmount - openDebitorAmount) < 1) {
                    PaymentId paymentId = savePaymentAndDebts(currentPaymentDebited, sortedCreditor.get(c), currentDebtBuilders);
                    paymentsCharged.add(paymentId);
                    currentPaymentDebited = 0.;
                    currentDebtBuilders = new ArrayList<>();
                    c++;
                }
            } else if (openCreditorAmount == openDebitorAmount) {
                currentPaymentDebited += openCreditorAmount;
                Debt.DebtBuilder debtToAdd = Debt.builder()
                        .amount(openCreditorAmount)
                        .userId(sortedDebitor.get(d).getKey())
                        .status(DebtStatus.OPEN);
                currentDebtBuilders.add(debtToAdd);
                PaymentId paymentId = savePaymentAndDebts(currentPaymentDebited, sortedCreditor.get(c), currentDebtBuilders);
                paymentsCharged.add(paymentId);
                currentPaymentDebited = 0.;
                currentDebtBuilders = new ArrayList<>();
                c++;
                d++;
            } else {
                currentPaymentDebited += openCreditorAmount;
                sortedDebitor.get(d).setValue(openDebitorAmount - openCreditorAmount);
                Debt.DebtBuilder debtToAdd = Debt.builder()
                        .amount(openCreditorAmount)
                        .userId(sortedDebitor.get(d).getKey())
                        .status(DebtStatus.OPEN);
                currentDebtBuilders.add(debtToAdd);
                PaymentId paymentId = savePaymentAndDebts(currentPaymentDebited, sortedCreditor.get(c), currentDebtBuilders);
                paymentsCharged.add(paymentId);
                currentPaymentDebited = 0.;
                currentDebtBuilders = new ArrayList<>();
                c++;
                if ((openDebitorAmount - openCreditorAmount) < 1) {
                    d++;
                }
            }
        }

        return paymentsCharged;
    }

    private PaymentId savePaymentAndDebts(double currentPaymentDebited, Map.Entry<UserId, Double> creditor, List<Debt.DebtBuilder> currentDebtBuilders) {
        Payment newPayment = Payment.builder()
                .amount(currentPaymentDebited)
                .userId(creditor.getKey())
                .status(PaymentStatus.CHARGED)
                .fuelPayment(false)
                .payedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .reason("Benzin Ausgleichszahlung")
                .build();
        PaymentId paymentId = paymentRepository.savePayment(newPayment);
        debtRepository.saveDebts(currentDebtBuilders.stream()
                .map(debtBuilder -> {
                    debtBuilder.paymentId(paymentId);
                    return debtBuilder.build();
                })
                .toList());

        return paymentId;
    }

    private static void calculateOpenPayments(List<UserDTO> users, Map<UserId, Double> hoursPerUser, double totalHours, double totalPayed, Map<UserId, Double> payedAmountPerUsers, Map<UserId, Double> debitor, Map<UserId, Double> creditor) {
        for (UserDTO userDTO : users) {
            Double userHours = hoursPerUser.getOrDefault(userDTO.userId(), 0.);
            double percentHours = userHours / totalHours;
            double shouldPay = percentHours * totalPayed;
            Double hasPayed = payedAmountPerUsers.getOrDefault(userDTO.userId(), 0.);
            double paymentDifference = Math.round((hasPayed - shouldPay) * 100.) / 100.;
            if (paymentDifference < 0) {
                debitor.put(userDTO.userId(), -1 * paymentDifference);
            } else {
                creditor.put(userDTO.userId(), paymentDifference);
            }
        }
    }

    private static Map<UserId, Double> mapPayedAmountToUsers(List<PaymentWithUsername> paymentsForPeriod) {
        Map<UserId, Double> payedPerUser = new HashMap<>();
        for (PaymentWithUsername payment : paymentsForPeriod) {
            payedPerUser.merge(payment.creditor().userId(), payment.amount(), Double::sum);
        }
        return payedPerUser;
    }

    private static Map<UserId, Double> mapHoursToUsers(List<ReservationUserDTO> reservationsForPeriod) {
        Map<UserId, Double> hoursPerUser = new HashMap<>();
        for (ReservationUserDTO reservationUserDTO : reservationsForPeriod) {
            double hours;
            if (reservationUserDTO.boatHoursOnStart() == null || reservationUserDTO.boatHoursOnEnd() == null) {
                Duration duration = Duration.between(reservationUserDTO.startDateTime(), reservationUserDTO.endDateTime());
                hours = duration.toMinutes() / 60.;
            } else {
                hours = reservationUserDTO.boatHoursOnEnd() - reservationUserDTO.boatHoursOnStart();
            }
            hoursPerUser.merge(reservationUserDTO.userDTO().userId(), hours, Double::sum);
        }
        return hoursPerUser;
    }
}
