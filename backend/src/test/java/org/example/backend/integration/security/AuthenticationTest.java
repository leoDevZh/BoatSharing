package org.example.backend.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.TestDBConfiguration;
import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.repository.user.User;
import org.example.backend.infrastructure.security.controller.AuthenticationRequestTO;
import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

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
public class AuthenticationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private CustomUserDetail customUserDetail;

    @BeforeEach
    void setup() {
        user = User.builder()
                .username(UUID.randomUUID().toString().substring(0, 8))
                .password("$2a$12$GbqlJ0fs.00G5MLiCA9AZuA0QF000rXCrkQ0sP5EkGbwsOb.KmlEm") // password123
                .build();
        userRepository.save(user);
        customUserDetail = CustomUserDetail.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    @Nested
    class AuthenticateTests {
        @Test
        void authenticateUserOnSuccess() throws Exception {
            AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
            authenticationRequestTO.setUsername(user.getUsername());
            authenticationRequestTO.setPassword("password123");
            String authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequestTO);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationRequestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        void authenticateUserOnWrongPassword() throws Exception {
            AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
            authenticationRequestTO.setUsername(user.getUsername());
            authenticationRequestTO.setPassword("someOtherPassword");
            String authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequestTO);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationRequestJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }

        @Test
        void authenticateUserOnWrongUsername() throws Exception {
            AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
            authenticationRequestTO.setUsername("otherUser");
            authenticationRequestTO.setPassword("password123");
            String authenticationRequestJson = objectMapper.writeValueAsString(authenticationRequestTO);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationRequestJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }
    }

    @Nested
    class RefreshTests {
        @Test
        void refreshOnSuccess() throws Exception {
            mockMvc.perform(post("/api/auth/refresh")
                            .with(user(customUserDetail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        void refreshOnNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }
    }
}
