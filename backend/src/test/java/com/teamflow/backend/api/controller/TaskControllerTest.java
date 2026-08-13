package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.api.exception.GlobalExceptionHandler;
import com.teamflow.backend.application.service.TaskService;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskController taskController = new TaskController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTask_whenValid_returnsCreated201() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskResponse response = new TaskResponse(
                taskId, projectId, null, null, "Setup Database", "Task desc", TaskStatus.TODO, TaskPriority.HIGH, Instant.now()
        );
        given(taskService.createTask(any(TaskCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"title\":\"Setup Database\",\"description\":\"Task desc\",\"status\":\"TODO\",\"priority\":\"HIGH\"}", projectId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tasks/" + taskId))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Setup Database"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createTask_whenProjectNotFound_returnsNotFound404() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(taskService.createTask(any(TaskCreateRequest.class)))
                .willThrow(new ResourceNotFoundException("Project not found with id: " + projectId));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"title\":\"Setup Database\",\"status\":\"TODO\",\"priority\":\"HIGH\"}", projectId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTask_whenSprintBelongsToDifferentProject_returnsBadRequest400() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(taskService.createTask(any(TaskCreateRequest.class)))
                .willThrow(new IllegalArgumentException("Sprint does not belong to project"));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"projectId\":\"%s\",\"title\":\"Setup Database\",\"status\":\"TODO\",\"priority\":\"HIGH\"}", projectId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Sprint does not belong to project"));
    }

    @Test
    void getTaskById_whenExists_returnsOk200() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskResponse response = new TaskResponse(
                taskId, projectId, null, null, "Setup Database", "Task desc", TaskStatus.TODO, TaskPriority.HIGH, Instant.now()
        );
        given(taskService.getTaskById(taskId)).willReturn(response);

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Setup Database"));
    }

    @Test
    void getTaskById_whenNotFound_returnsNotFound404() throws Exception {
        UUID taskId = UUID.randomUUID();
        given(taskService.getTaskById(taskId))
                .willThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTask_whenValid_returnsOk200() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskResponse response = new TaskResponse(
                taskId, projectId, null, null, "Setup Database Updated", "Task desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, Instant.now()
        );
        given(taskService.updateTask(eq(taskId), any(TaskUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Setup Database Updated\",\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Setup Database Updated"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTask_whenExists_returnsNoContent204() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
    }
}
