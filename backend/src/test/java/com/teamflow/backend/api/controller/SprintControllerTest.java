package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.SprintCreateRequest;
import com.teamflow.backend.api.dto.SprintResponse;
import com.teamflow.backend.api.dto.SprintUpdateRequest;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.SprintService;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.SprintStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SprintControllerTest {

    @Mock
    private SprintService sprintService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SprintController sprintController = new SprintController(sprintService);
        mockMvc = MockMvcBuilders.standaloneSetup(sprintController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createSprint_whenValid_returnsCreated201() throws Exception {
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        SprintResponse response = new SprintResponse(
                sprintId, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now()
        );
        given(sprintService.createSprint(any(SprintCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"name\":\"Sprint 1\",\"startDate\":\"2026-08-14\",\"endDate\":\"2026-08-28\",\"status\":\"PLANNED\"}", projectId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sprints/" + sprintId))
                .andExpect(jsonPath("$.id").value(sprintId.toString()))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void createSprint_whenProjectNotFound_returnsNotFound404() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(sprintService.createSprint(any(SprintCreateRequest.class)))
                .willThrow(new ResourceNotFoundException("Project not found with id: " + projectId));

        mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"name\":\"Sprint 1\",\"startDate\":\"2026-08-14\",\"endDate\":\"2026-08-28\",\"status\":\"PLANNED\"}", projectId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createSprint_whenInvalidDates_returnsBadRequest400() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(sprintService.createSprint(any(SprintCreateRequest.class)))
                .willThrow(new IllegalArgumentException("End date must not be before start date"));

        mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"name\":\"Sprint 1\",\"startDate\":\"2026-08-28\",\"endDate\":\"2026-08-14\",\"status\":\"PLANNED\"}", projectId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("End date must not be before start date"));
    }

    @Test
    void getSprintById_whenExists_returnsOk200() throws Exception {
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        SprintResponse response = new SprintResponse(
                sprintId, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now()
        );
        given(sprintService.getSprintById(sprintId)).willReturn(response);

        mockMvc.perform(get("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sprintId.toString()))
                .andExpect(jsonPath("$.name").value("Sprint 1"));
    }

    @Test
    void getSprintById_whenNotFound_returnsNotFound404() throws Exception {
        UUID sprintId = UUID.randomUUID();
        given(sprintService.getSprintById(sprintId))
                .willThrow(new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        mockMvc.perform(get("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateSprint_whenValid_returnsOk200() throws Exception {
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        SprintResponse response = new SprintResponse(
                sprintId, projectId, "Sprint 1 Updated", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.ACTIVE, Instant.now()
        );
        given(sprintService.updateSprint(eq(sprintId), any(SprintUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/sprints/{id}", sprintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sprint 1 Updated\",\"startDate\":\"2026-08-14\",\"endDate\":\"2026-08-28\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1 Updated"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void deleteSprint_whenExists_returnsNoContent204() throws Exception {
        UUID sprintId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isNoContent());
    }
}
