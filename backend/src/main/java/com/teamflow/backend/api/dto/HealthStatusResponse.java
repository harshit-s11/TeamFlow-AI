package com.teamflow.backend.api.dto;

import java.time.LocalDateTime;

public record HealthStatusResponse(
        String status,
        String service,
        String database,
        LocalDateTime timestamp
) {
    public static HealthStatusResponse of(String status, String service, String database) {
        return new HealthStatusResponse(status, service, database, LocalDateTime.now());
    }
}
