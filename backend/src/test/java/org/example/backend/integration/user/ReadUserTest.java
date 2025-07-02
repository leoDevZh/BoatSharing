package org.example.backend.integration.user;

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
public class ReadUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaBoatRepository boatRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User user1;
    private User user2;
    private Boat boat1;
    private Boat boat2;
    private CustomUserDetail customUserDetail;

    @BeforeEach
    void setup() {
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
        boat2 = Boat.builder()
                .name("Ms Norwegian")
                .userIds(Set.of(user2.getId()))
                .build();
        boatRepository.save(boat1);
        boatRepository.save(boat2);
        customUserDetail = CustomUserDetail.builder()
                .id(user1.getId())
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();
    }

    @Test
    void findUsersByBoatIdOnSuccess() throws Exception {
        mockMvc.perform(get("/api/user/all-users-boat")
                        .param("boatId", boat1.getId().toString())
                        .with(user(customUserDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value(user1.getUsername()))
                .andExpect(jsonPath("$[0].userId.value").value(user1.getId()));
    }

    @Test
    void findUsersByBoatIdOnBoatIdNotExists() throws Exception {
        mockMvc.perform(get("/api/user/all-users-boat")
                        .param("boatId", "999")
                        .with(user(customUserDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findUsersByBoatIdOnInvalidParam() throws Exception {
        mockMvc.perform(get("/api/user/all-users-boat")
                        .param("someParam", "abc")
                        .with(user(customUserDetail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findUsersByBoatIdOnBoatIdInvalidValue() throws Exception {
        mockMvc.perform(get("/api/user/all-users-boat")
                        .param("boatId", "abc")
                        .with(user(customUserDetail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Provide a valid Variable"));
    }

    @Test
    void findUsersByBoatIdOnNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/all-users-boat")
                        .param("boatId", "123"))
                .andExpect(status().isForbidden());
    }
}
