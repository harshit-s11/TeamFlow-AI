package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
    public static Project create(String name, String description) {
        return new Project(null, name, description, Instant.now());
    }
}
