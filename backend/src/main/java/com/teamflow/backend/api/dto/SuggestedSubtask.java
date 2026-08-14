package com.teamflow.backend.api.dto;

import com.teamflow.backend.domain.model.TaskPriority;

public record SuggestedSubtask(
        String title,
        String description,
        TaskPriority priority,
        Integer estimatedStoryPoints
) {}
