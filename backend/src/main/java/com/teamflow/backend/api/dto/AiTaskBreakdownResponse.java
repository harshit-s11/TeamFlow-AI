package com.teamflow.backend.api.dto;

import java.util.List;
import java.util.UUID;

public record AiTaskBreakdownResponse(
        UUID parentTaskId,
        List<SuggestedSubtask> suggestedSubtasks
) {}
