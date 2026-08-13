package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TaskCreateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.api.dto.TaskUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.Task;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.UserRepository;
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
        if (projectRepository.findById(request.projectId()).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + request.projectId());
        }

        if (request.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.sprintId()));

            if (!sprint.projectId().equals(request.projectId())) {
                throw new IllegalArgumentException("Sprint with id " + request.sprintId() + " does not belong to project " + request.projectId());
            }
        }

        if (request.assignedUserId() != null) {
            if (userRepository.findById(request.assignedUserId()).isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + request.assignedUserId());
            }
        }

        Task task = Task.create(
                request.projectId(),
                request.sprintId(),
                request.assignedUserId(),
                request.title(),
                request.description(),
                request.status(),
                request.priority()
        );

        Task saved = taskRepository.save(task);
        return TaskResponse.fromDomain(saved);
    }

    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return TaskResponse.fromDomain(task);
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    public List<TaskResponse> getTasksByProjectId(UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    public List<TaskResponse> getTasksBySprintId(UUID sprintId) {
        if (sprintRepository.findById(sprintId).isEmpty()) {
            throw new ResourceNotFoundException("Sprint not found with id: " + sprintId);
        }

        return taskRepository.findBySprintId(sprintId)
                .stream()
                .map(TaskResponse::fromDomain)
                .toList();
    }

    public TaskResponse updateTask(UUID id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (request.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.sprintId()));

            if (!sprint.projectId().equals(existingTask.projectId())) {
                throw new IllegalArgumentException("Sprint with id " + request.sprintId() + " does not belong to project " + existingTask.projectId());
            }
        }

        if (request.assignedUserId() != null) {
            if (userRepository.findById(request.assignedUserId()).isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + request.assignedUserId());
            }
        }

        Task updatedTask = new Task(
                existingTask.id(),
                existingTask.projectId(),
                request.sprintId(),
                request.assignedUserId(),
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                existingTask.createdAt()
        );

        Task saved = taskRepository.save(updatedTask);
        return TaskResponse.fromDomain(saved);
    }

    public void deleteTask(UUID id) {
        if (taskRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}
