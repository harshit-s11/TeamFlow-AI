package com.teamflow.backend.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AiTaskBreakdownRequest(
        @Min(1) @Max(10) Integer targetSubtaskCount
) {
    public Integer getEffectiveTargetSubtaskCount() {
        return (targetSubtaskCount != null && targetSubtaskCount > 0) ? targetSubtaskCount : 4;
    }
}
