package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TaskActivityLog(
        UUID id,
        UUID projectId,
        UUID taskId,
        UUID actorUserId,
        String eventType,
        String fieldChanged,
        String oldValue,
        String newValue,
        Instant createdAt
) {
    public static TaskActivityLog create(
            UUID projectId,
            UUID taskId,
            UUID actorUserId,
            String eventType,
            String fieldChanged,
            String oldValue,
            String newValue
    ) {
        return new TaskActivityLog(
                null,
                projectId,
                taskId,
                actorUserId,
                eventType,
                fieldChanged,
                oldValue,
                newValue,
                Instant.now()
        );
    }
}
