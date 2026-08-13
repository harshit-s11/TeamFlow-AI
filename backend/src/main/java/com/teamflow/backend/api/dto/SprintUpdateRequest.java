package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SprintUpdateRequest(
        @NotBlank(message = "Sprint name is required")
        @Size(max = 255, message = "Sprint name must not exceed 255 characters")
        String name,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "Status is required")
        SprintStatus status
) {}
