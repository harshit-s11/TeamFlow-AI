package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.Team;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        Instant createdAt
) {
    public static TeamResponse fromDomain(Team team) {
        return new TeamResponse(team.id(), team.name(), team.createdAt());
    }
}
