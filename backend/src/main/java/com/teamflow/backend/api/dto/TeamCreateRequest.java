package com.teamflow.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 255, message = "Team name must not exceed 255 characters")
        String name
) {}
