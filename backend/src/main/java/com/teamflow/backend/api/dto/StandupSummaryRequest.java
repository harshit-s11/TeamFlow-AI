package com.teamflow.backend.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StandupSummaryRequest(
        @Min(1) @Max(168) Integer timeWindowHours
) {
    public Integer getEffectiveTimeWindowHours() {
        return (timeWindowHours != null && timeWindowHours > 0) ? timeWindowHours : 24;
    }
}
