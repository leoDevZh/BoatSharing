package org.example.backend.integration.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.TestDBConfiguration;
import org.example.backend.domain.reservation.ReservationId;
import org.example.backend.infrastructure.controller.reservation.UpdateEngineHoursDTO;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
public class UpdateEngineHoursForReservationTest {
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
    private Reservation reservation1;
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
        reservation1 = Reservation.builder()
                .userId(user1.getId())
                .boatId(boat1.getId())
                .startDateTime(LocalDateTime.of(2025, 03, 28, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 03, 28, 12, 0))
                .build();
        reservationRepository.save(reservation1);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
    }

    @Test
    void updateEngineHoursForReservationOnSuccess() throws Exception {
        Integer engineHoursOnStart = 200;
        Integer engineHoursOnEnd = 250;
        UpdateEngineHoursDTO updateEngineHoursDTO = new UpdateEngineHoursDTO(
                engineHoursOnStart,
                engineHoursOnEnd,
                new ReservationId(reservation1.getId())
        );
        String updateEngineHoursDTOJson = objectMapper.writeValueAsString(updateEngineHoursDTO);

        mockMvc.perform(post("/api/reservation/updateEngineHours")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEngineHoursDTOJson))
                .andExpect(status().isOk());
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        assertEquals(reservation1.getId(), reservation.getId());
        assertEquals(reservation1.getUserId(), reservation.getUserId());
        assertEquals(reservation1.getBoatId(), reservation.getBoatId());
        assertEquals(reservation1.getStartDateTime(), reservation.getStartDateTime());
        assertEquals(reservation1.getEndDateTime(), reservation.getEndDateTime());
        assertEquals(engineHoursOnStart, reservation.getBoatHoursOnStart());
        assertEquals(engineHoursOnEnd, reservation.getBoatHoursOnEnd());
    }

    @Test
    void updateEngineHoursForReservationOnUserNotOwner() throws Exception {
        Integer engineHoursOnStart = 200;
        Integer engineHoursOnEnd = 250;
        UpdateEngineHoursDTO updateEngineHoursDTO = new UpdateEngineHoursDTO(
                engineHoursOnStart,
                engineHoursOnEnd,
                new ReservationId(reservation1.getId())
        );
        String updateEngineHoursDTOJson = objectMapper.writeValueAsString(updateEngineHoursDTO);
        CustomUserDetail customUserDetail2 = CustomUserDetail.builder()
                .id(user1.getId() + 1L)
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();

        mockMvc.perform(post("/api/reservation/updateEngineHours")
                        .with(user(customUserDetail2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEngineHoursDTOJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not owner of reservation"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        assertEquals(reservation1.getId(), reservation.getId());
        assertEquals(reservation1.getUserId(), reservation.getUserId());
        assertEquals(reservation1.getBoatId(), reservation.getBoatId());
        assertEquals(reservation1.getStartDateTime(), reservation.getStartDateTime());
        assertEquals(reservation1.getEndDateTime(), reservation.getEndDateTime());
        assertNull(reservation.getBoatHoursOnStart());
        assertNull(reservation.getBoatHoursOnEnd());
    }

    @Test
    void updateEngineHoursForReservationOnReservationNotExisting() throws Exception {
        Integer engineHoursOnStart = 200;
        Integer engineHoursOnEnd = 250;
        UpdateEngineHoursDTO updateEngineHoursDTO = new UpdateEngineHoursDTO(
                engineHoursOnStart,
                engineHoursOnEnd,
                new ReservationId(reservation1.getId() + 1L)
        );
        String updateEngineHoursDTOJson = objectMapper.writeValueAsString(updateEngineHoursDTO);

        mockMvc.perform(post("/api/reservation/updateEngineHours")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEngineHoursDTOJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reservation not found"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        assertEquals(reservation1.getId(), reservation.getId());
        assertEquals(reservation1.getUserId(), reservation.getUserId());
        assertEquals(reservation1.getBoatId(), reservation.getBoatId());
        assertEquals(reservation1.getStartDateTime(), reservation.getStartDateTime());
        assertEquals(reservation1.getEndDateTime(), reservation.getEndDateTime());
        assertNull(reservation.getBoatHoursOnStart());
        assertNull(reservation.getBoatHoursOnEnd());
    }

    @Test
    void updateEngineHoursForReservationOnEnginHoursStartGreaterThenEnd() throws Exception {
        Integer engineHoursOnStart = 200;
        Integer engineHoursOnEnd = 150;
        UpdateEngineHoursDTO updateEngineHoursDTO = new UpdateEngineHoursDTO(
                engineHoursOnStart,
                engineHoursOnEnd,
                new ReservationId(reservation1.getId())
        );
        String updateEngineHoursDTOJson = objectMapper.writeValueAsString(updateEngineHoursDTO);

        mockMvc.perform(post("/api/reservation/updateEngineHours")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEngineHoursDTOJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start hours is greater then end hours"));
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        assertEquals(reservation1.getId(), reservation.getId());
        assertEquals(reservation1.getUserId(), reservation.getUserId());
        assertEquals(reservation1.getBoatId(), reservation.getBoatId());
        assertEquals(reservation1.getStartDateTime(), reservation.getStartDateTime());
        assertEquals(reservation1.getEndDateTime(), reservation.getEndDateTime());
        assertNull(reservation.getBoatHoursOnStart());
        assertNull(reservation.getBoatHoursOnEnd());
    }

    @Test
    void createReservationOnNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/reservation/updateEngineHours"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestBodiesForupdateEngineHoursForReservation")
    void updateEngineHoursForReservationOnEnginHoursOnInvalidBody(String jsonRequest) throws Exception {
        mockMvc.perform(post("/api/reservation/updateEngineHours")
                        .with(user(customUserDetail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Provide a valid Requestbody"));
    }

    static Stream<String> invalidRequestBodiesForupdateEngineHoursForReservation() {
        return Stream.of(
                "{\"boatEngineHoursOnStart\":\"150\",\"boatEngineHoursOnEnd\":\"200\"}",
                "{\"boatEngineHoursOnStart\":\"150\",\"reservationId\":{\"value\":123}}",
                "{\"boatEngineHoursOnEnd\":\"200\",\"reservationId\":{\"value\":123}}"
        );
    }
}
