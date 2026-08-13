package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.Task;
import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            SprintRepository sprintRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
    }

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

    public TaskResponse updateTask(UUID id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        checkProjectMemberOrAdmin(existingTask.projectId());

        validateForeignKeys(existingTask.projectId(), request.sprintId(), request.assignedUserId());

        TaskStatus status = request.status() != null ? request.status() : existingTask.status();
        TaskPriority priority = request.priority() != null ? request.priority() : existingTask.priority();

        Task taskToSave = new Task(
                existingTask.id(),
                existingTask.projectId(),
                request.sprintId(),
                request.assignedUserId(),
                request.title(),
                request.description(),
                status,
                priority,
                existingTask.createdAt()
        );

        Task updatedTask = taskRepository.save(taskToSave);
        return TaskResponse.fromDomain(updatedTask);
    }

    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        checkProjectMemberOrAdmin(task.projectId());
        taskRepository.deleteById(id);
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
