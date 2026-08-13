package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Team(
        UUID id,
        String name,
        Instant createdAt
) {
    public static Team create(String name) {
        return new Team(null, name, Instant.now());
    }
}
