package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.*;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, projectRepository, sprintRepository, userRepository);
    }

    @Test
    void createTask_whenValid_returnsTaskResponse() {
        UUID projectId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, null, "Implement API", "Task desc", TaskStatus.TODO, TaskPriority.HIGH
        );
        UUID taskId = UUID.randomUUID();
        Task savedTask = new Task(taskId, projectId, null, null, "Implement API", "Task desc", TaskStatus.TODO, TaskPriority.HIGH, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.id()).isEqualTo(taskId);
        assertThat(response.title()).isEqualTo("Implement API");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void createTask_whenProjectNotFound_throwsResourceNotFoundException() {
        UUID projectId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(projectId.toString());
    }

    @Test
    void createTask_whenSprintBelongsToDifferentProject_throwsIllegalArgumentException() {
        UUID projectId = UUID.randomUUID();
        UUID otherProjectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, sprintId, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(new Sprint(sprintId, otherProjectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now())));

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to project");
    }

    @Test
    void createTask_whenUserNotFound_throwsResourceNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, userId, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void getTaskById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(taskRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateTask_whenValid_returnsUpdatedResponse() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskUpdateRequest request = new TaskUpdateRequest(
                null, null, "Task Updated", "New desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH
        );
        Task existingTask = new Task(taskId, projectId, null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());
        Task updatedTask = new Task(taskId, projectId, null, null, "Task Updated", "New desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, existingTask.createdAt());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(taskRepository.save(any(Task.class))).willReturn(updatedTask);

        TaskResponse response = taskService.updateTask(taskId, request);

        assertThat(response.title()).isEqualTo("Task Updated");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_whenExists_deletesTask() {
        UUID id = UUID.randomUUID();
        Task existingTask = new Task(id, UUID.randomUUID(), null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(id)).willReturn(Optional.of(existingTask));

        taskService.deleteTask(id);

        verify(taskRepository).deleteById(id);
    }

    @Test
    void getTasksByProjectId_whenProjectExists_returnsTaskList() {
        UUID projectId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), projectId, null, null, "Task 1", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(taskRepository.findByProjectId(projectId)).willReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasksByProjectId(projectId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Task 1");
    }

    @Test
    void getTasksBySprintId_whenSprintExists_returnsTaskList() {
        UUID sprintId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), UUID.randomUUID(), sprintId, null, "Task 1", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(new Sprint(sprintId, UUID.randomUUID(), "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now())));
        given(taskRepository.findBySprintId(sprintId)).willReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasksBySprintId(sprintId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Task 1");
    }
}
