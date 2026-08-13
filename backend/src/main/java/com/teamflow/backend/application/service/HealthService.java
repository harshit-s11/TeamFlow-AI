package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.HealthStatusResponse;
import com.teamflow.backend.repository.SystemHealthRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final String SERVICE_NAME = "teamflow-backend";
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";

    private final SystemHealthRepository systemHealthRepository;

    public HealthService(SystemHealthRepository systemHealthRepository) {
        this.systemHealthRepository = systemHealthRepository;
    }

    public HealthStatusResponse getHealthStatus() {
        boolean dbConnected = systemHealthRepository.isDatabaseConnected();
        String dbStatus = dbConnected ? STATUS_UP : STATUS_DOWN;
        String overallStatus = dbConnected ? STATUS_UP : STATUS_DOWN;

        return HealthStatusResponse.of(overallStatus, SERVICE_NAME, dbStatus);
    }
}
