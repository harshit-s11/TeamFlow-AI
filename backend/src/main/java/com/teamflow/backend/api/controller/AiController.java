package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.*;
import com.teamflow.backend.application.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/tasks/{id}/breakdown")
    public ResponseEntity<AiTaskBreakdownResponse> generateTaskBreakdown(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) AiTaskBreakdownRequest request
    ) {
        int count = request != null ? request.getEffectiveTargetSubtaskCount() : 4;
        AiTaskBreakdownResponse response = aiService.generateTaskBreakdown(id, count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sprints/{id}/forecast")
    public ResponseEntity<SprintVelocityForecastResponse> forecastSprintVelocity(
            @PathVariable UUID id
    ) {
        SprintVelocityForecastResponse response = aiService.forecastSprintVelocity(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/projects/{id}/standup-summary")
    public ResponseEntity<StandupSummaryResponse> generateStandupSummary(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) StandupSummaryRequest request
    ) {
        int hours = request != null ? request.getEffectiveTimeWindowHours() : 24;
        StandupSummaryResponse response = aiService.generateStandupSummary(id, hours);
        return ResponseEntity.ok(response);
    }
}
