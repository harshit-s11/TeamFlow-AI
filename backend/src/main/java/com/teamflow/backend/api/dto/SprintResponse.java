package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        UUID projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        Instant createdAt
) {
    public static SprintResponse fromDomain(Sprint sprint) {
        return new SprintResponse(
                sprint.id(),
                sprint.projectId(),
                sprint.name(),
                sprint.startDate(),
                sprint.endDate(),
                sprint.status(),
                sprint.createdAt()
        );
    }
}
