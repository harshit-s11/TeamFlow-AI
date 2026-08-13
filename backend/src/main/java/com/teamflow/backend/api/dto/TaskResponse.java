package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.Task;
import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID sprintId,
        UUID assignedUserId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt
) {
    public static TaskResponse fromDomain(Task task) {
        return new TaskResponse(
                task.id(),
                task.projectId(),
                task.sprintId(),
                task.assignedUserId(),
                task.title(),
                task.description(),
                task.status(),
                task.priority(),
                task.createdAt()
        );
    }
}
