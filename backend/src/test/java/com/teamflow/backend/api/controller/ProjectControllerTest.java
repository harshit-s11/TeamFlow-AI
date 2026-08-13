package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.ProjectCreateRequest;
import com.teamflow.backend.api.dto.ProjectResponse;
import com.teamflow.backend.api.dto.ProjectUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.ProjectService;
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
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProjectController projectController = new ProjectController(projectService);
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createProject_whenValid_returnsCreated201() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(projectId, "TeamFlow Core", "Platform engine", Instant.now());
        given(projectService.createProject(any(ProjectCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TeamFlow Core\",\"description\":\"Platform engine\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/projects/" + projectId))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("TeamFlow Core"))
                .andExpect(jsonPath("$.description").value("Platform engine"));
    }

    @Test
    void getProjectById_whenExists_returnsOk200() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(projectId, "TeamFlow Core", "Platform engine", Instant.now());
        given(projectService.getProjectById(projectId)).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("TeamFlow Core"));
    }

    @Test
    void getProjectById_whenNotFound_returnsNotFound404() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(projectService.getProjectById(projectId))
                .willThrow(new ResourceNotFoundException("Project not found with id: " + projectId));

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateProject_whenValid_returnsOk200() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(projectId, "TeamFlow Core v2", "Updated desc", Instant.now());
        given(projectService.updateProject(eq(projectId), any(ProjectUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TeamFlow Core v2\",\"description\":\"Updated desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TeamFlow Core v2"));
    }

    @Test
    void deleteProject_whenExists_returnsNoContent204() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                .andExpect(status().isNoContent());
    }

    @Test
    void addMember_whenValid_returnsCreated201() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isCreated());
    }

    @Test
    void addMember_whenDuplicate_returnsConflict409() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new DuplicateResourceException("User is already a member"))
                .when(projectService).addMember(projectId, userId);

        mockMvc.perform(post("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void removeMember_whenExists_returnsNoContent204() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeMember_whenNotMember_returnsNotFound404() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("User is not a member"))
                .when(projectService).removeMember(projectId, userId);

        mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getProjectMembers_whenProjectExists_returnsOk200() throws Exception {
        UUID projectId = UUID.randomUUID();
        UserResponse user = new UserResponse(UUID.randomUUID(), "Leo", "leo@teamflow.com", Instant.now());
        given(projectService.getProjectMembers(projectId)).willReturn(List.of(user));

        mockMvc.perform(get("/api/v1/projects/{projectId}/members", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Leo"))
                .andExpect(jsonPath("$[0].email").value("leo@teamflow.com"));
    }
}
