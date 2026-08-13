package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TaskCreateRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        UUID sprintId,

        UUID assignedUserId,

        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title must not exceed 255 characters")
        String title,

        String description,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        @NotNull(message = "Task priority is required")
        TaskPriority priority
) {}
