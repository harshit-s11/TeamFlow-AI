package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
    public static ProjectResponse fromDomain(Project project) {
        return new ProjectResponse(project.id(), project.name(), project.description(), project.createdAt());
    }
}
