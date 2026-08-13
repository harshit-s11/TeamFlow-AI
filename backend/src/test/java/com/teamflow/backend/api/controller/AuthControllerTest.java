package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.AuthResponse;
import com.teamflow.backend.api.dto.LoginRequest;
import com.teamflow.backend.api.dto.RegisterRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.AuthService;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_whenValid_returnsCreated201() throws Exception {
        UserResponse user = new UserResponse(UUID.randomUUID(), "Alice", "alice@teamflow.com", Instant.now());
        AuthResponse response = new AuthResponse("token123", user);

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@teamflow.com\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.user.email").value("alice@teamflow.com"));
    }

    @Test
    void register_whenDuplicateEmail_returnsConflict409() throws Exception {
        given(authService.register(any(RegisterRequest.class)))
                .willThrow(new DuplicateResourceException("Email is already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@teamflow.com\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_whenInvalidValidation_returnsBadRequest400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalid-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_whenValidCredentials_returnsOk200() throws Exception {
        UserResponse user = new UserResponse(UUID.randomUUID(), "Alice", "alice@teamflow.com", Instant.now());
        AuthResponse response = new AuthResponse("token123", user);

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@teamflow.com\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.user.email").value("alice@teamflow.com"));
    }

    @Test
    void login_whenInvalidCredentials_returnsUnauthorized401() throws Exception {
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@teamflow.com\",\"password\":\"WrongPassword!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
