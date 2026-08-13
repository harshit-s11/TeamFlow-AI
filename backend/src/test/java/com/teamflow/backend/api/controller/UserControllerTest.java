package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.UserCreateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.dto.UserUpdateRequest;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.UserService;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createUser_whenValid_returnsCreated201() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(userId, "Henry", "henry@teamflow.com", Instant.now());
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Henry\",\"email\":\"henry@teamflow.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/" + userId))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Henry"))
                .andExpect(jsonPath("$.email").value("henry@teamflow.com"));
    }

    @Test
    void createUser_whenDuplicateEmail_returnsConflict409() throws Exception {
        given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new DuplicateResourceException("User already exists with email: henry@teamflow.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Henry\",\"email\":\"henry@teamflow.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createUser_whenValidationFails_returnsBadRequest400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalid-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getUserById_whenExists_returnsOk200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(userId, "Henry", "henry@teamflow.com", Instant.now());
        given(userService.getUserById(userId)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Henry"));
    }

    @Test
    void getUserById_whenNotFound_returnsNotFound404() throws Exception {
        UUID userId = UUID.randomUUID();
        given(userService.getUserById(userId))
                .willThrow(new ResourceNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateUser_whenValid_returnsOk200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(userId, "Henry Updated", "henry.new@teamflow.com", Instant.now());
        given(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Henry Updated\",\"email\":\"henry.new@teamflow.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Henry Updated"))
                .andExpect(jsonPath("$.email").value("henry.new@teamflow.com"));
    }

    @Test
    void deleteUser_whenExists_returnsNoContent204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenNotFound_returnsNotFound404() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found with id: " + userId))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
