package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.HealthStatusResponse;
import com.teamflow.backend.application.service.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private HealthService healthService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HealthController healthController = new HealthController(healthService);
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    @Test
    void getHealth_whenStatusUp_returnsOk() throws Exception {
        given(healthService.getHealthStatus())
                .willReturn(HealthStatusResponse.of("UP", "teamflow-backend", "UP"));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("teamflow-backend"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getHealth_whenStatusDown_returnsServiceUnavailable() throws Exception {
        given(healthService.getHealthStatus())
                .willReturn(HealthStatusResponse.of("DOWN", "teamflow-backend", "DOWN"));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.database").value("DOWN"));
    }
}
