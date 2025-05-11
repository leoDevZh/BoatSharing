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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDBConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
public class CancelReservationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaUserRepository userRepository;

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
                .startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3))
                .build();
        reservationRepository.save(reservation1);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
    }

    @Test
    void cancelReservationOnSuccess() throws Exception {
        mockMvc.perform(delete("/api/reservation/cancel/" + reservation1.getId())
                        .with(user(customUserDetail)))
                .andExpect(status().isNoContent());
        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    void cancelReservationOnReservationIdNotFound() throws Exception {
        mockMvc.perform(delete("/api/reservation/cancel/" + (reservation1.getId() + 1L))
                        .with(user(customUserDetail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reservation not found"));
        assertEquals(1, reservationRepository.findAll().size());
    }

    @Test
    void cancelReservationOnUserNotOwner() throws Exception {
        CustomUserDetail otherUser = CustomUserDetail.builder()
                .id(user1.getId() + 1L)
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
        mockMvc.perform(delete("/api/reservation/cancel/" + reservation1.getId())
                        .with(user(otherUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not owner of reservation"));
        assertEquals(1, reservationRepository.findAll().size());
    }

    @Test
    void cancelReservationOnReservationInPast() throws Exception {
        Reservation reservation = Reservation.builder()
                .userId(user1.getId())
                .boatId(boat1.getId())
                .startDateTime(LocalDateTime.now().minusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3))
                .build();
        reservationRepository.save(reservation);

        mockMvc.perform(delete("/api/reservation/cancel/" + reservation.getId())
                        .with(user(customUserDetail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Can not delete reservation anymore"));
        assertEquals(2, reservationRepository.findAll().size());
    }

    @Test
    void cancelReservationOnNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/reservation/cancel/" + reservation1.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "' '",
            "'a'",
            "'1ld2'"
    })
    void cancelReservationOnInvalidPathVariable(String pathVariable) throws Exception {
        mockMvc.perform(delete("/api/reservation/cancel/" + pathVariable)
                        .with(user(customUserDetail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
    }
}
