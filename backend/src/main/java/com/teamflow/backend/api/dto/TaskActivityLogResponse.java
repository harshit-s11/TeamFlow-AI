package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.TaskActivityLog;

import java.time.Instant;
import java.util.UUID;

public record TaskActivityLogResponse(
        UUID id,
        UUID projectId,
        UUID taskId,
        UUID actorUserId,
        String actorName,
        String eventType,
        String fieldChanged,
        String oldValue,
        String newValue,
        Instant createdAt
) {
    public static TaskActivityLogResponse fromDomain(TaskActivityLog log, String actorName) {
        return new TaskActivityLogResponse(
                log.id(),
                log.projectId(),
                log.taskId(),
                log.actorUserId(),
                actorName,
                log.eventType(),
                log.fieldChanged(),
                log.oldValue(),
                log.newValue(),
                log.createdAt()
        );
    }
}
