package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Instant createdAt
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
    }
}
