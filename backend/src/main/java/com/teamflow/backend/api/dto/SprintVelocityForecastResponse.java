package com.teamflow.backend.api.dto;

import java.util.List;
import java.util.UUID;

public record SprintVelocityForecastResponse(
        UUID sprintId,
        Double historicalAverageVelocity,
        Double plannedCapacity,
        Double forecastedCompletionRate,
        String riskLevel,
        List<String> aiInsights
) {}
