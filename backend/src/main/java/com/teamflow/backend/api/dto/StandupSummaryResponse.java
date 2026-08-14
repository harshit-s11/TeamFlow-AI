package com.teamflow.backend.api.dto;

import java.util.List;
import java.util.UUID;

public record StandupSummaryResponse(
        UUID projectId,
        Integer timeWindowHours,
        List<String> completedWork,
        List<String> inProgressWork,
        List<String> blockersAndRisks,
        String generatedSummary
) {}
