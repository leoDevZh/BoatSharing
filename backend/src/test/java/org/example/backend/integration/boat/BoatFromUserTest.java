package org.example.backend.integration.boat;

import org.example.backend.TestDBConfiguration;
import org.example.backend.infrastructure.repository.boat.Boat;
import org.example.backend.infrastructure.repository.boat.JpaBoatRepository;
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
public class BoatFromUserTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private Boat boat;
    private User user1;
    private User user2;
    private CustomUserDetail customUserDetail1;
    private CustomUserDetail customUserDetail2;

    @BeforeEach
    void setUp() {
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
        boat = Boat.builder()
                .name("Das Boot")
                .userIds(Set.of(user1.getId()))
                .build();
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
        boatRepository.save(boat);
    }

    @Test
    void getBoatIdFromUserOnSuccess() throws Exception {
        mockMvc.perform(get("/api/boat")
                        .with(user(customUserDetail1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(boat.getId()));
    }

    @Test
    void getBoatIdFromUserOnUserNotOwner() throws Exception {
        mockMvc.perform(get("/api/boat")
                        .with(user(customUserDetail2)))
                .andExpect(status().isBadRequest());
    }
}
