package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.HealthStatusResponse;
import com.teamflow.backend.repository.SystemHealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private SystemHealthRepository systemHealthRepository;

    private HealthService healthService;

    @BeforeEach
    void setUp() {
        healthService = new HealthService(systemHealthRepository);
    }

    @Test
    void getHealthStatus_whenDatabaseConnected_returnsStatusUp() {
        given(systemHealthRepository.isDatabaseConnected()).willReturn(true);

        HealthStatusResponse response = healthService.getHealthStatus();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.service()).isEqualTo("teamflow-backend");
        assertThat(response.database()).isEqualTo("UP");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void getHealthStatus_whenDatabaseNotConnected_returnsStatusDown() {
        given(systemHealthRepository.isDatabaseConnected()).willReturn(false);

        HealthStatusResponse response = healthService.getHealthStatus();

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.service()).isEqualTo("teamflow-backend");
        assertThat(response.database()).isEqualTo("DOWN");
        assertThat(response.timestamp()).isNotNull();
    }
}
