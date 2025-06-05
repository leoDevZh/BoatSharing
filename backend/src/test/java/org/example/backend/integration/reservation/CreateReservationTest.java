package org.example.backend.integration.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.TestDBConfiguration;
import org.example.backend.domain.boat.BoatId;
import org.example.backend.infrastructure.controller.reservation.CreateReservationDTO;
import org.example.backend.infrastructure.repository.boat.Boat;
import org.example.backend.infrastructure.repository.boat.JpaBoatRepository;
import org.example.backend.infrastructure.repository.reservation.JpaReservationRepository;
import org.example.backend.infrastructure.repository.reservation.Reservation;
import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.repository.user.User;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
public class CreateReservationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private Boat boat1;
    private CustomUserDetail customUserDetail;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        boatRepository.deleteAll();
        userRepository.deleteAll();
        user1 = User.builder()
                .username("Captain")
                .password("pwd")
                .build();
        userRepository.save(user1);
        boat1 = Boat.builder()
                .name("Atlantica")
                .userIds(Set.of(user1.getId()))
                .build();
        boatRepository.save(boat1);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
    }

    @Test
    void createReservationOnSuccess() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(boat1.getId())
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isCreated());
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        assertEquals(start, reservation.getStartDateTime());
        assertEquals(end, reservation.getEndDateTime());
        assertEquals(boat1.getId(), reservation.getBoatId());
        assertEquals(user1.getId(), reservation.getUserId());
        assertNull(reservation.getBoatHoursOnStart());
        assertNull(reservation.getBoatHoursOnEnd());
    }

    @Test
    void createReservationOnBoatNotExisting() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(43L)
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Boat not found"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(0, reservations.size());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidReservationDates")
    void createReservationOnStartAfterEnd(LocalDateTime start, LocalDateTime end) throws Exception {
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(boat1.getId())
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start time is after end time"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(0, reservations.size());
    }

    @Test
    void createReservationOnUserIsNotOwner() throws Exception {
        Boat boat2 = Boat.builder().name("Atlantica 2").build();
        boatRepository.save(boat2);
        LocalDateTime start = LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(boat2.getId())
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not co-owner of boat"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(0, reservations.size());
    }

    @ParameterizedTest
    @MethodSource("provideReservationData")
    void createReservationOnOverlappingReservation(Reservation.ReservationBuilder existingReservationBuilder) throws Exception {
        Reservation existingReservation = existingReservationBuilder
                .boatId(boat1.getId())
                .userId(user1.getId())
                .build();
        reservationRepository.save(existingReservation);
        LocalDateTime start = LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(boat1.getId())
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reservation overlap with an existing reservation"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
    }

    @Test
    void createReservationOnNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/reservation/create"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestBodies")
    void createReservationOnInvalidBody(String jsonRequest) throws Exception {
        mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Provide a valid Requestbody"));
    }

    @Test
    void createReservationOnConcurrentCall() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS);
        CreateReservationDTO createReservationDTO = new CreateReservationDTO(
                start,
                end,
                new BoatId(boat1.getId())
        );
        String reservationJson = objectMapper.writeValueAsString(createReservationDTO);

        CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(() -> {
            try {
                return mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ResultActions> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return mockMvc.perform(post("/api/reservation/create")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        int statusOne = future.join().andReturn().getResponse().getStatus();
        int statusTwo = future2.join().andReturn().getResponse().getStatus();
        assertTrue(statusOne != statusTwo);
        assertTrue(List.of(statusOne, statusTwo).contains(HttpStatus.CREATED.value()));
        assertTrue(List.of(statusOne, statusTwo).contains(HttpStatus.CONFLICT.value()));
    }

    static Stream<String> invalidRequestBodies() {
        return Stream.of(
                "{\"startTime\":\"2025-03-23T10:00:00\",\"endTime\":\"2025-03-23T12:00:00\"}",
                "{\"boatId\":{\"value\":123},\"endTime\":\"2025-03-23T12:00:00\"}",
                "{\"boatId\":{\"value\":123},\"startTime\":\"2025-03-23T10:00:00\"}"
        );
    }

    private static Stream<Arguments> provideReservationData() {
        return Stream.of(
                // Overlapping endTime
                Arguments.of(
                        Reservation.builder()
                                .startDateTime(LocalDateTime.now().plusHours(9).truncatedTo(ChronoUnit.SECONDS))
                                .endDateTime(LocalDateTime.now().plusHours(12).truncatedTo(ChronoUnit.SECONDS))
                ),
                // Overlapping startTime
                Arguments.of(
                        Reservation.builder()
                                .startDateTime(LocalDateTime.now().plusHours(12).truncatedTo(ChronoUnit.SECONDS))
                                .endDateTime(LocalDateTime.now().plusHours(16).truncatedTo(ChronoUnit.SECONDS))
                ),
                // Overlapping start- and endTime
                Arguments.of(
                        Reservation.builder()
                                .startDateTime(LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.SECONDS))
                                .endDateTime(LocalDateTime.now().plusHours(15).truncatedTo(ChronoUnit.SECONDS))
                )
        );
    }

    private static Stream<Arguments> provideInvalidReservationDates() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        return Stream.of(
                Arguments.of(now.plusHours(2), now),
                Arguments.of(now, now)
        );
    }
}
