package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TaskActivityLogResponse;
import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.Task;
import com.teamflow.backend.domain.model.TaskActivityLog;
import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskActivityLogRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskActivityLogRepository activityLogRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            SprintRepository sprintRepository,
            UserRepository userRepository,
            TaskActivityLogRepository activityLogRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));

        checkProjectMemberOrAdmin(request.projectId());

        validateForeignKeys(request.projectId(), request.sprintId(), request.assignedUserId());

        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        TaskPriority priority = request.priority() != null ? request.priority() : TaskPriority.MEDIUM;

        Task task = Task.create(
                request.projectId(),
                request.sprintId(),
                request.assignedUserId(),
                request.title(),
                request.description(),
                status,
                priority
        );

        Task savedTask = taskRepository.save(task);

        // Record TASK_CREATED audit log event
        UUID actorUserId = SecurityUtils.getCurrentUserId();
        TaskActivityLog createdLog = TaskActivityLog.create(
                savedTask.projectId(),
                savedTask.id(),
                actorUserId,
                "TASK_CREATED",
                null,
                null,
                savedTask.title()
        );
        activityLogRepository.save(createdLog);

        return TaskResponse.fromDomain(savedTask);
    }

    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        checkProjectMemberOrAdmin(task.projectId());
        return TaskResponse.fromDomain(task);
    }

    public List<TaskResponse> getTasksByProjectId(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        checkProjectMemberOrAdmin(projectId);

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    public List<TaskResponse> getTasksBySprintId(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        checkProjectMemberOrAdmin(sprint.projectId());

        return taskRepository.findBySprintId(sprintId)
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    public List<TaskResponse> getAllTasks() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    @Transactional
    public TaskResponse updateTask(UUID id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        checkProjectMemberOrAdmin(existingTask.projectId());

        validateForeignKeys(existingTask.projectId(), request.sprintId(), request.assignedUserId());

        TaskStatus newStatus = request.status() != null ? request.status() : existingTask.status();
        TaskPriority newPriority = request.priority() != null ? request.priority() : existingTask.priority();

        // Enforce task status transition state machine rules if status changed
        if (newStatus != existingTask.status()) {
            validateStatusTransition(existingTask.status(), newStatus);
        }

        Task taskToSave = new Task(
                existingTask.id(),
                existingTask.projectId(),
                request.sprintId(),
                request.assignedUserId(),
                request.title(),
                request.description(),
                newStatus,
                newPriority,
                existingTask.createdAt()
        );

        Task updatedTask = taskRepository.save(taskToSave);
        UUID actorUserId = SecurityUtils.getCurrentUserId();

        // Audit field-specific changes
        if (newStatus != existingTask.status()) {
            activityLogRepository.save(TaskActivityLog.create(
                    updatedTask.projectId(),
                    updatedTask.id(),
                    actorUserId,
                    "STATUS_CHANGED",
                    "status",
                    existingTask.status().name(),
                    newStatus.name()
            ));
        }

        if (newPriority != existingTask.priority()) {
            activityLogRepository.save(TaskActivityLog.create(
                    updatedTask.projectId(),
                    updatedTask.id(),
                    actorUserId,
                    "PRIORITY_CHANGED",
                    "priority",
                    existingTask.priority().name(),
                    newPriority.name()
            ));
        }

        if (!Objects.equals(request.assignedUserId(), existingTask.assignedUserId())) {
            activityLogRepository.save(TaskActivityLog.create(
                    updatedTask.projectId(),
                    updatedTask.id(),
                    actorUserId,
                    "ASSIGNEE_CHANGED",
                    "assignedUserId",
                    existingTask.assignedUserId() != null ? existingTask.assignedUserId().toString() : null,
                    request.assignedUserId() != null ? request.assignedUserId().toString() : null
            ));
        }

        if (!Objects.equals(request.sprintId(), existingTask.sprintId())) {
            activityLogRepository.save(TaskActivityLog.create(
                    updatedTask.projectId(),
                    updatedTask.id(),
                    actorUserId,
                    "SPRINT_CHANGED",
                    "sprintId",
                    existingTask.sprintId() != null ? existingTask.sprintId().toString() : null,
                    request.sprintId() != null ? request.sprintId().toString() : null
            ));
        }

        return TaskResponse.fromDomain(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        checkProjectMemberOrAdmin(task.projectId());

        // Record TASK_DELETED audit log event BEFORE task row deletion
        UUID actorUserId = SecurityUtils.getCurrentUserId();
        activityLogRepository.save(TaskActivityLog.create(
                task.projectId(),
                task.id(),
                actorUserId,
                "TASK_DELETED",
                null,
                task.title(),
                null
        ));

        taskRepository.deleteById(id);
    }

    public List<TaskActivityLogResponse> getTaskActivity(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectMemberOrAdmin(task.projectId());

        List<TaskActivityLog> logs = activityLogRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return logs.stream().map(log -> {
            String actorName = userRepository.findById(log.actorUserId())
                    .map(User::name)
                    .orElse("Unknown User");
            return TaskActivityLogResponse.fromDomain(log, actorName);
        }).toList();
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TaskStatus.IN_REVIEW || newStatus == TaskStatus.TODO;
            case IN_REVIEW -> newStatus == TaskStatus.DONE || newStatus == TaskStatus.IN_PROGRESS;
            case DONE -> newStatus == TaskStatus.IN_PROGRESS;
        };

        if (!valid) {
            throw new IllegalArgumentException("Invalid task status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void validateForeignKeys(UUID projectId, UUID sprintId, UUID assignedUserId) {
        if (sprintId != null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

            if (!sprint.projectId().equals(projectId)) {
                throw new IllegalArgumentException("Sprint does not belong to project with id: " + projectId);
            }
        }

        if (assignedUserId != null) {
            userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + assignedUserId));
        }
    }

    private void checkProjectMemberOrAdmin(UUID projectId) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!projectRepository.isMember(projectId, userId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
