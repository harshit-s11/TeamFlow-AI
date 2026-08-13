package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Sprint(
        UUID id,
        UUID projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        Instant createdAt
) {
    public static Sprint create(UUID projectId, String name, LocalDate startDate, LocalDate endDate, SprintStatus status) {
        return new Sprint(null, projectId, name, startDate, endDate, status, Instant.now());
    }
}
