package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.TeamService;
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
class TeamControllerTest {

    @Mock
    private TeamService teamService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TeamController teamController = new TeamController(teamService);
        mockMvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTeam_whenValid_returnsCreated201() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamResponse response = new TeamResponse(teamId, "Backend Devs", Instant.now());
        given(teamService.createTeam(any(TeamCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Backend Devs\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/teams/" + teamId))
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Backend Devs"));
    }

    @Test
    void getTeamById_whenExists_returnsOk200() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamResponse response = new TeamResponse(teamId, "Backend Devs", Instant.now());
        given(teamService.getTeamById(teamId)).willReturn(response);

        mockMvc.perform(get("/api/v1/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Backend Devs"));
    }

    @Test
    void getTeamById_whenNotFound_returnsNotFound404() throws Exception {
        UUID teamId = UUID.randomUUID();
        given(teamService.getTeamById(teamId))
                .willThrow(new ResourceNotFoundException("Team not found with id: " + teamId));

        mockMvc.perform(get("/api/v1/teams/{id}", teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTeam_whenValid_returnsOk200() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamResponse response = new TeamResponse(teamId, "Updated Team Name", Instant.now());
        given(teamService.updateTeam(eq(teamId), any(TeamUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/teams/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Team Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Team Name"));
    }

    @Test
    void deleteTeam_whenExists_returnsNoContent204() throws Exception {
        UUID teamId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/teams/{id}", teamId))
                .andExpect(status().isNoContent());
    }

    @Test
    void addMember_whenValid_returnsCreated201() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isCreated());
    }

    @Test
    void addMember_whenDuplicate_returnsConflict409() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new DuplicateResourceException("User is already a member"))
                .when(teamService).addMember(teamId, userId);

        mockMvc.perform(post("/api/v1/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void removeMember_whenExists_returnsNoContent204() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeMember_whenNotMember_returnsNotFound404() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("User is not a member"))
                .when(teamService).removeMember(teamId, userId);

        mockMvc.perform(delete("/api/v1/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getTeamMembers_whenTeamExists_returnsOk200() throws Exception {
        UUID teamId = UUID.randomUUID();
        UserResponse user = new UserResponse(UUID.randomUUID(), "Jack", "jack@teamflow.com", Instant.now());
        given(teamService.getTeamMembers(teamId)).willReturn(List.of(user));

        mockMvc.perform(get("/api/v1/teams/{teamId}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jack"))
                .andExpect(jsonPath("$[0].email").value("jack@teamflow.com"));
    }
}
