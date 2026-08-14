package com.teamflow.backend.application.service;

import tools.jackson.databind.ObjectMapper;
import com.teamflow.backend.api.dto.AiTaskBreakdownResponse;
import com.teamflow.backend.api.dto.SprintVelocityForecastResponse;
import com.teamflow.backend.api.dto.StandupSummaryResponse;
import com.teamflow.backend.common.exception.AiServiceUnavailableException;
import com.teamflow.backend.domain.model.*;
import com.teamflow.backend.infrastructure.ai.GeminiApiClient;
import com.teamflow.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class AiServiceTest {

    @Mock
    private GeminiApiClient geminiApiClient;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskActivityLogRepository activityLogRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AiService aiService;

    private AutoCloseable closeable;
    private UUID currentUserId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        aiService = new AiService(geminiApiClient, taskRepository, sprintRepository, projectRepository, activityLogRepository, objectMapper);

        currentUserId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        setAuthUser(currentUserId, "USER");
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        if (closeable != null) {
            closeable.close();
        }
    }

    private void setAuthUser(UUID userId, String role) {
        UserAccount userAccount = new UserAccount(userId, "John Doe", "user@example.com", "hash", role, Instant.now());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                userAccount, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
        SecurityContextHolder.setContext(context);
    }


    @Test
    void generateTaskBreakdown_success() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task(taskId, projectId, null, null, "Parent Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);

        String mockGeminiJson = """
                {
                  "suggestedSubtasks": [
                    { "title": "Subtask 1", "description": "Desc 1", "priority": "HIGH", "estimatedStoryPoints": 3 },
                    { "title": "Subtask 2", "description": "Desc 2", "priority": "LOW", "estimatedStoryPoints": 1 }
                  ]
                }
                """;
        given(geminiApiClient.generateContent(any(), any())).willReturn(mockGeminiJson);

        AiTaskBreakdownResponse response = aiService.generateTaskBreakdown(taskId, 2);

        assertNotNull(response);
        assertEquals(taskId, response.parentTaskId());
        assertEquals(2, response.suggestedSubtasks().size());
        assertEquals("Subtask 1", response.suggestedSubtasks().get(0).title());
        assertEquals(TaskPriority.HIGH, response.suggestedSubtasks().get(0).priority());
    }

    @Test
    void generateTaskBreakdown_whenNotProjectMember_throwsAccessDeniedException() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task(taskId, projectId, null, null, "Parent Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(false);

        assertThrows(AccessDeniedException.class, () -> aiService.generateTaskBreakdown(taskId, 4));
    }

    @Test
    void forecastSprintVelocity_whenNoCompletedSprints_returnsDeterministicFallback() {
        UUID sprintId = UUID.randomUUID();
        Sprint currentSprint = new Sprint(sprintId, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now());

        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(currentSprint));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);
        given(sprintRepository.findByProjectId(projectId)).willReturn(List.of(currentSprint));
        given(taskRepository.findBySprintId(sprintId)).willReturn(List.of());

        SprintVelocityForecastResponse response = aiService.forecastSprintVelocity(sprintId);

        assertNotNull(response);
        assertEquals(0.0, response.historicalAverageVelocity());
        assertEquals("MEDIUM", response.riskLevel());
        assertTrue(response.aiInsights().get(0).contains("Insufficient sprint history"));
    }

    @Test
    void forecastSprintVelocity_whenCompletedSprintsExist_calculatesMetricsAndInsights() {
        UUID sprintId = UUID.randomUUID();
        UUID compSprintId = UUID.randomUUID();

        Sprint currentSprint = new Sprint(sprintId, projectId, "Sprint 2", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.ACTIVE, Instant.now());
        Sprint completedSprint = new Sprint(compSprintId, projectId, "Sprint 1", LocalDate.now().minusDays(14), LocalDate.now(), SprintStatus.COMPLETED, Instant.now());

        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(currentSprint));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);
        given(sprintRepository.findByProjectId(projectId)).willReturn(List.of(currentSprint, completedSprint));

        Task compTask1 = new Task(UUID.randomUUID(), projectId, compSprintId, null, "Task 1", "Desc", TaskStatus.DONE, TaskPriority.MEDIUM, Instant.now());
        given(taskRepository.findBySprintId(compSprintId)).willReturn(List.of(compTask1));
        given(taskRepository.findBySprintId(sprintId)).willReturn(List.of());

        String mockInsightJson = """
                {
                  "riskLevel": "LOW",
                  "aiInsights": ["Sprint capacity is well aligned with historical performance."]
                }
                """;
        given(geminiApiClient.generateContent(any(), any())).willReturn(mockInsightJson);

        SprintVelocityForecastResponse response = aiService.forecastSprintVelocity(sprintId);

        assertNotNull(response);
        assertEquals(1.0, response.historicalAverageVelocity());
        assertEquals("LOW", response.riskLevel());
        assertEquals(1, response.aiInsights().size());
    }

    @Test
    void generateStandupSummary_success() {
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);
        given(activityLogRepository.findByProjectIdAndWindow(eq(projectId), any())).willReturn(List.of());
        given(taskRepository.findByProjectId(projectId)).willReturn(List.of());

        String mockSummaryJson = """
                {
                  "generatedSummary": "### Standup Summary\\n- All systems running smoothly."
                }
                """;
        given(geminiApiClient.generateContent(any(), any())).willReturn(mockSummaryJson);

        StandupSummaryResponse response = aiService.generateStandupSummary(projectId, 24);

        assertNotNull(response);
        assertEquals(projectId, response.projectId());
        assertEquals(24, response.timeWindowHours());
        assertTrue(response.generatedSummary().contains("Standup Summary"));
    }

    @Test
    void generateTaskBreakdown_whenApiKeyMissing_throwsAiServiceUnavailableException() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task(taskId, projectId, null, null, "Parent Task", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, Instant.now());

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectRepository.isMember(projectId, currentUserId)).willReturn(true);
        given(geminiApiClient.generateContent(any(), any())).willThrow(new AiServiceUnavailableException("AI service is not configured"));

        assertThrows(AiServiceUnavailableException.class, () -> aiService.generateTaskBreakdown(taskId, 3));
    }
}
