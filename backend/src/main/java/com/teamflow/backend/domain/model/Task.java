package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Task(
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
    public static Task create(
            UUID projectId,
            UUID sprintId,
            UUID assignedUserId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority
    ) {
        return new Task(null, projectId, sprintId, assignedUserId, title, description, status, priority, Instant.now());
    }
}
