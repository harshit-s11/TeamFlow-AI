package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String name,
        String email,
        Instant createdAt
) {
    public static User create(String name, String email) {
        return new User(null, name, email, Instant.now());
    }
}
