package com.teamflow.backend.domain.model;

import java.time.Instant;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String name,
        String email,
        String passwordHash,
        String role,
        Instant createdAt
) {}
