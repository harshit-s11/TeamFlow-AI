package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TaskActivityLogResponse;
import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.domain.model.*;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskActivityLogRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Mock
    private TaskActivityLogRepository activityLogRepository;

    private TaskService taskService;
    private UUID currentUserId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                projectRepository,
                sprintRepository,
                userRepository,
                activityLogRepository
        );

        currentUserId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        UserAccount userAccount = new UserAccount(currentUserId, "John Doe", "user@example.com", "hash", "USER", Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userAccount,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createTask_savesTaskAndRecordsTaskCreatedAuditLog() {
        TaskCreateRequest request = new TaskCreateRequest(
                projectId, null, null, "Build API", "Initial setup", TaskStatus.TODO, TaskPriority.HIGH
        );

        Project project = new Project(projectId, "Platform", "Desc", Instant.now());
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        Task savedTask = new Task(
                UUID.randomUUID(), projectId, null, null, "Build API", "Initial setup",
                TaskStatus.TODO, TaskPriority.HIGH, Instant.now()
        );
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        TaskActivityLog savedLog = new TaskActivityLog(
                UUID.randomUUID(), projectId, savedTask.id(), currentUserId,
                "TASK_CREATED", null, null, "Build API", Instant.now()
        );
        given(activityLogRepository.save(any(TaskActivityLog.class))).willReturn(savedLog);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Build API", response.title());

        ArgumentCaptor<TaskActivityLog> logCaptor = ArgumentCaptor.forClass(TaskActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        TaskActivityLog capturedLog = logCaptor.getValue();
        assertEquals("TASK_CREATED", capturedLog.eventType());
        assertEquals(currentUserId, capturedLog.actorUserId());
        assertEquals("Build API", capturedLog.newValue());
    }

    @Test
    void updateTask_validStatusTransition_succeedsAndLogsStatusChanged() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, null, "Build API", "Desc",
                TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        Task updatedTask = new Task(
                taskId, projectId, null, null, "Build API", "Desc",
                TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, Instant.now()
        );
        given(taskRepository.save(any(Task.class))).willReturn(updatedTask);

        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                null, null, "Build API", "Desc", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM
        );

        TaskResponse response = taskService.updateTask(taskId, updateRequest);

        assertEquals(TaskStatus.IN_PROGRESS, response.status());

        ArgumentCaptor<TaskActivityLog> logCaptor = ArgumentCaptor.forClass(TaskActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        TaskActivityLog log = logCaptor.getValue();
        assertEquals("STATUS_CHANGED", log.eventType());
        assertEquals("TODO", log.oldValue());
        assertEquals("IN_PROGRESS", log.newValue());
        assertEquals(currentUserId, log.actorUserId());
    }

    @Test
    void updateTask_invalidStatusTransition_throwsIllegalArgumentException() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, null, "Build API", "Desc",
                TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        TaskUpdateRequest invalidRequest = new TaskUpdateRequest(
                null, null, "Build API", "Desc", TaskStatus.DONE, TaskPriority.MEDIUM
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, invalidRequest)
        );

        assertTrue(ex.getMessage().contains("Invalid task status transition from TODO to DONE"));
        verify(activityLogRepository, never()).save(any());
    }

    @Test
    void updateTask_urgentPriority_doesNotModifyAssignedUserId() {
        UUID taskId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, assigneeId, "Urgent Task", "Desc",
                TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        User assignee = new User(assigneeId, "assignee@example.com", "Assignee User", Instant.now());
        given(userRepository.findById(assigneeId)).willReturn(Optional.of(assignee));

        Task updatedTask = new Task(
                taskId, projectId, null, assigneeId, "Urgent Task", "Desc",
                TaskStatus.TODO, TaskPriority.URGENT, Instant.now()
        );
        given(taskRepository.save(any(Task.class))).willReturn(updatedTask);

        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                null, assigneeId, "Urgent Task", "Desc", TaskStatus.TODO, TaskPriority.URGENT
        );

        TaskResponse response = taskService.updateTask(taskId, updateRequest);

        assertEquals(TaskPriority.URGENT, response.priority());
        assertEquals(assigneeId, response.assignedUserId());
    }

    @Test
    void deleteTask_recordsTaskDeletedAuditLogBeforeDeletion() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, null, "Task to Delete", "Desc",
                TaskStatus.TODO, TaskPriority.LOW, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        taskService.deleteTask(taskId);

        ArgumentCaptor<TaskActivityLog> logCaptor = ArgumentCaptor.forClass(TaskActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());
        TaskActivityLog log = logCaptor.getValue();

        assertEquals("TASK_DELETED", log.eventType());
        assertEquals(taskId, log.taskId());
        assertEquals("Task to Delete", log.oldValue());
        assertEquals(currentUserId, log.actorUserId());

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void getTaskActivity_whenProjectMember_returnsActivityLogs() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, null, "Task 1", "Desc",
                TaskStatus.TODO, TaskPriority.LOW, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        TaskActivityLog log1 = new TaskActivityLog(
                UUID.randomUUID(), projectId, taskId, currentUserId,
                "TASK_CREATED", null, null, "Task 1", Instant.now()
        );
        given(activityLogRepository.findByTaskIdOrderByCreatedAtAsc(taskId)).willReturn(List.of(log1));

        User actor = new User(currentUserId, "John Doe", "john@example.com", Instant.now());
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(actor));

        List<TaskActivityLogResponse> activities = taskService.getTaskActivity(taskId);

        assertEquals(1, activities.size());
        assertEquals("TASK_CREATED", activities.get(0).eventType());
        assertEquals("John Doe", activities.get(0).actorName());
    }

    @Test
    void getTaskActivity_whenNotProjectMember_throwsAccessDeniedException() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(
                taskId, projectId, null, null, "Task 1", "Desc",
                TaskStatus.TODO, TaskPriority.LOW, Instant.now()
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskActivity(taskId));
    }
}
