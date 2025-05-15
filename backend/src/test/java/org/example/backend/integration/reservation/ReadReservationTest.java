package org.example.backend.integration.reservation;

import org.example.backend.TestDBConfiguration;
import org.example.backend.infrastructure.repository.boat.Boat;
import org.example.backend.infrastructure.repository.boat.JpaBoatRepository;
import org.example.backend.infrastructure.repository.reservation.JpaReservationRepository;
import org.example.backend.infrastructure.repository.reservation.Reservation;
import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.repository.user.User;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;

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
public class ReadReservationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User user1;
    private User user2;
    private Boat boat1;
    private Reservation reservation1;
    private CustomUserDetail customUserDetail1;
    private CustomUserDetail customUserDetail2;

    private final LocalDateTime baseDateTime = LocalDateTime.now();

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        boatRepository.deleteAll();
        userRepository.deleteAll();
        user1 = User.builder()
                .username("Captain")
                .password("pwd")
                .build();
        user2 = User.builder()
                .username("Cadette")
                .password("pwd")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        boat1 = Boat.builder()
                .name("Atlantica")
                .userIds(Set.of(user1.getId()))
                .build();
        boatRepository.save(boat1);
        reservation1 = Reservation.builder()
                .userId(user1.getId())
                .boatId(boat1.getId())
                .startDateTime(baseDateTime.withNano(0))
                .endDateTime(baseDateTime.plusHours(2).withNano(0))
                .build();
        reservationRepository.save(reservation1);
        customUserDetail1 = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
        customUserDetail2 = CustomUserDetail.builder()
                .id(user2.getId())
                .username(user2.getUsername())
                .password(user2.getPassword())
                .build();
    }

    @Test
    void findReservationForPeriodOnSuccess() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().toString())
                        .param("to", reservation1.getEndDateTime().toString())
                        .param("boatId", boat1.getId().toString())
                        .with(user(customUserDetail1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reservationId.value").value(reservation1.getId().toString()))
                .andExpect(jsonPath("$[0].startDateTime").value(reservation1.getStartDateTime().toString()))
                .andExpect(jsonPath("$[0].endDateTime").value(reservation1.getEndDateTime().toString()))
                .andExpect(jsonPath("$[0].boatHoursOnStart").value(reservation1.getBoatHoursOnStart()))
                .andExpect(jsonPath("$[0].boatHoursOnEnd").value(reservation1.getBoatHoursOnEnd()))
                .andExpect(jsonPath("$[0].boatId.value").value(boat1.getId().toString()))
                .andExpect(jsonPath("$[0].userDTO.userId.value").value(user1.getId().toString()))
                .andExpect(jsonPath("$[0].userDTO.username").value(user1.getUsername()));
    }

    @Test
    void findNoReservationForPeriodBefore() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().minusDays(1).toString())
                        .param("to", reservation1.getStartDateTime().toString())
                        .param("boatId", boat1.getId().toString())
                        .with(user(customUserDetail1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findNoReservationForPeriodAfter() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getEndDateTime().toString())
                        .param("to", reservation1.getEndDateTime().plusDays(1).toString())
                        .param("boatId", boat1.getId().toString())
                        .with(user(customUserDetail1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findReservationForPeriodOnUserNotOwner() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().toString())
                        .param("to", reservation1.getEndDateTime().toString())
                        .param("boatId", boat1.getId().toString())
                        .with(user(customUserDetail2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not co-owner of boat"));
    }

    @Test
    void findReservationForPeriodOnBoatNotFound() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().toString())
                        .param("to", reservation1.getEndDateTime().toString())
                        .param("boatId", "123")
                        .with(user(customUserDetail1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Boat not found"));
    }

    @Test
    void findReservationForPeriodOnMissingParam() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().toString())
                        .param("to", reservation1.getEndDateTime().toString())
                        .with(user(customUserDetail2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findReservationForPeriodOnNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/read-reservation")
                        .param("from", reservation1.getStartDateTime().toString())
                        .param("to", reservation1.getEndDateTime().toString())
                        .param("boatId", boat1.getId().toString()))
                .andExpect(status().isForbidden());
    }
}
