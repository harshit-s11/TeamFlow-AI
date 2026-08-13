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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private void setAuthUser(UUID userId, String role) {
        UserAccount account = new UserAccount(userId, "Test User", "test@teamflow.com", "hash", role, Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, projectRepository, sprintRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_whenProjectMember_returnsTaskResponse() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, null, "Implement API", "Task desc", TaskStatus.TODO, TaskPriority.HIGH
        );
        UUID taskId = UUID.randomUUID();
        Task savedTask = new Task(taskId, projectId, null, null, "Implement API", "Task desc", TaskStatus.TODO, TaskPriority.HIGH, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.id()).isEqualTo(taskId);
        assertThat(response.title()).isEqualTo("Implement API");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void createTask_whenNonProjectMember_throwsAccessDeniedException() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createTask_whenSprintBelongsToDifferentProject_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID otherProjectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, sprintId, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(new Sprint(sprintId, otherProjectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now())));

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to project");
    }

    @Test
    void getTaskById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        given(taskRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getTaskById_whenAssignedUserButNonProjectMember_throwsAccessDeniedException() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Task task = new Task(taskId, projectId, null, userId, "Task Title", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectRepository.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateTask_whenProjectMember_returnsUpdatedResponse() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        TaskUpdateRequest request = new TaskUpdateRequest(
                null, null, "Task Updated", "New desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH
        );
        Task existingTask = new Task(taskId, projectId, null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());
        Task updatedTask = new Task(taskId, projectId, null, null, "Task Updated", "New desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, existingTask.createdAt());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(taskRepository.save(any(Task.class))).willReturn(updatedTask);

        TaskResponse response = taskService.updateTask(taskId, request);

        assertThat(response.title()).isEqualTo("Task Updated");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_whenProjectMember_deletesTask() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Task existingTask = new Task(id, projectId, null, null, "Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(id)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);

        taskService.deleteTask(id);

        verify(taskRepository).deleteById(id);
    }

    @Test
    void getTasksByProjectId_whenProjectMember_returnsTaskList() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Task task = new Task(UUID.randomUUID(), projectId, null, null, "Task 1", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(taskRepository.findByProjectId(projectId)).willReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasksByProjectId(projectId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Task 1");
    }

    @Test
    void getTasksBySprintId_whenProjectMember_returnsTaskList() {
        UUID userId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Task task = new Task(UUID.randomUUID(), projectId, sprintId, null, "Task 1", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(new Sprint(sprintId, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(taskRepository.findBySprintId(sprintId)).willReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasksBySprintId(sprintId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Task 1");
    }

    @Test
    void getAllTasks_whenRegularUser_throwsAccessDeniedException() {
        setAuthUser(UUID.randomUUID(), "USER");

        assertThatThrownBy(() -> taskService.getAllTasks())
                .isInstanceOf(AccessDeniedException.class);
    }
}
