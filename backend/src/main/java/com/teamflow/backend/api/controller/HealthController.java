package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.HealthStatusResponse;
import com.teamflow.backend.application.service.HealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthStatusResponse> getHealth() {
        HealthStatusResponse healthStatus = healthService.getHealthStatus();
        HttpStatus httpStatus = "UP".equalsIgnoreCase(healthStatus.status()) 
                ? HttpStatus.OK 
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(healthStatus);
    }
}
