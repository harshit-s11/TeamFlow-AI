package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.SprintCreateRequest;
import com.teamflow.backend.api.dto.SprintResponse;
import com.teamflow.backend.api.dto.SprintUpdateRequest;
import com.teamflow.backend.api.dto.TaskResponse;
import com.teamflow.backend.application.service.SprintService;
import com.teamflow.backend.application.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sprints")
public class SprintController {

    private final SprintService sprintService;
    private final TaskService taskService;

    public SprintController(SprintService sprintService, TaskService taskService) {
        this.sprintService = sprintService;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> getAllSprints() {
        return ResponseEntity.ok(sprintService.getAllSprints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintResponse> getSprintById(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.getSprintById(id));
    }

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@Valid @RequestBody SprintCreateRequest request) {
        SprintResponse created = sprintService.createSprint(request);
        URI location = URI.create("/api/v1/sprints/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody SprintUpdateRequest request
    ) {
        return ResponseEntity.ok(sprintService.updateSprint(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable UUID id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sprintId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksBySprintId(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(taskService.getTasksBySprintId(sprintId));
    }
}
