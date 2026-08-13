package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.*;
import com.teamflow.backend.application.service.ProjectService;
import com.teamflow.backend.application.service.SprintService;
import com.teamflow.backend.application.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final SprintService sprintService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, SprintService sprintService, TaskService taskService) {
        this.projectService = projectService;
        this.sprintService = sprintService;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse created = projectService.createProject(request);
        URI location = URI.create("/api/v1/projects/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> addMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        projectService.addMember(projectId, userId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<UserResponse>> getProjectMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    @GetMapping("/{projectId}/sprints")
    public ResponseEntity<List<SprintResponse>> getSprintsByProjectId(@PathVariable UUID projectId) {
        return ResponseEntity.ok(sprintService.getSprintsByProjectId(projectId));
    }

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByProjectId(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
    }
}
