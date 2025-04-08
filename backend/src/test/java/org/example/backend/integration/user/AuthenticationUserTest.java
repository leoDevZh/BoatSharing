package org.example.backend.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.TestDBConfiguration;
import org.example.backend.infrastructure.controller.user.UserDTO;
import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.repository.user.User;
import org.example.backend.infrastructure.security.controller.AuthenticationRequestTO;
import org.example.backend.infrastructure.security.controller.AuthenticationResponseTO;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDBConfiguration.class)
@Testcontainers
@Transactional
public class AuthenticationUserTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaUserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user1 = User.builder()
                .username("Captain")
                .password("$2a$12$GbqlJ0fs.00G5MLiCA9AZuA0QF000rXCrkQ0sP5EkGbwsOb.KmlEm") // password123
                .build();
        user2 = User.builder()
                .username("Cadet")
                .password("$2a$12$/n1yDYl5yK28W558uvUY1OWGvkNErXPJkBTnwFyI7XLy7YOnyVGjC") // password456
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @ParameterizedTest
    @MethodSource("provideUser")
    void loginAndRetrieveUserOnSuccess(String username, String password) throws Exception {
        AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
        authenticationRequestTO.setUsername(username);
        authenticationRequestTO.setPassword(password);
        // Login
        String authenticationJson = objectMapper.writeValueAsString(authenticationRequestTO);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(authenticationJson))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        AuthenticationResponseTO response = objectMapper.readValue(jsonResponse, AuthenticationResponseTO.class);
        // Verify token
        result = mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer " + response.getToken()))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponse = result.getResponse().getContentAsString();
        UserDTO userDTO = objectMapper.readValue(jsonResponse, UserDTO.class);

        assertEquals(username, userDTO.getUsername());
    }

    @Test
    void loginAndRetrieveUserOnInvalidToken() throws Exception {
        AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
        authenticationRequestTO.setUsername(user1.getUsername());
        authenticationRequestTO.setPassword("password123");
        // Login
        String authenticationJson = objectMapper.writeValueAsString(authenticationRequestTO);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(authenticationJson))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        AuthenticationResponseTO response = objectMapper.readValue(jsonResponse, AuthenticationResponseTO.class);
        // Verify token
        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer " + response.getToken() + "invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.username").doesNotExist());
    }

    @Test
    void loginAndRetrieveUserOnMissingToken() throws Exception {
        AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
        authenticationRequestTO.setUsername(user1.getUsername());
        authenticationRequestTO.setPassword("password123");
        // Login
        String authenticationJson = objectMapper.writeValueAsString(authenticationRequestTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(authenticationJson))
                .andExpect(status().isOk());
        // Verify token
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.username").doesNotExist());
    }

    @Test
    void loginAndRetrieveUserOnInvalidUsername() throws Exception {
        AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
        authenticationRequestTO.setUsername("notExisting");
        authenticationRequestTO.setPassword("password123");
        // Login
        String authenticationJson = objectMapper.writeValueAsString(authenticationRequestTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(authenticationJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndRetrieveUserOnInvalidPassword() throws Exception {
        AuthenticationRequestTO authenticationRequestTO = new AuthenticationRequestTO();
        authenticationRequestTO.setUsername(user1.getUsername());
        authenticationRequestTO.setPassword("password456");
        // Login
        String authenticationJson = objectMapper.writeValueAsString(authenticationRequestTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(authenticationJson))
                .andExpect(status().isUnauthorized());
    }


    private static Stream<Arguments> provideUser() {
        return Stream.of(
                Arguments.of("Captain", "password123"),
                Arguments.of("Cadet", "password456")
        );
    }
}
