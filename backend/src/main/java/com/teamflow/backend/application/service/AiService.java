package com.teamflow.backend.application.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.teamflow.backend.api.dto.*;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.*;
import com.teamflow.backend.infrastructure.ai.GeminiApiClient;
import com.teamflow.backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final GeminiApiClient geminiApiClient;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public AiService(
            GeminiApiClient geminiApiClient,
            TaskRepository taskRepository,
            SprintRepository sprintRepository,
            ProjectRepository projectRepository,
            TaskActivityLogRepository activityLogRepository,
            ObjectMapper objectMapper
    ) {
        this.geminiApiClient = geminiApiClient;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
    }

    private void checkProjectMemberOrAdmin(UUID projectId) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!projectRepository.isMember(projectId, userId)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    public AiTaskBreakdownResponse generateTaskBreakdown(UUID taskId, Integer targetSubtaskCount) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        checkProjectMemberOrAdmin(task.projectId());

        int effectiveCount = (targetSubtaskCount != null && targetSubtaskCount > 0) ? targetSubtaskCount : 4;

        String systemInstruction = """
                You are an expert Agile Scrum Master and Software Architect.
                Analyze the task provided and break it down into smaller, concrete subtasks.
                You MUST return a raw JSON object with key "suggestedSubtasks" containing an array of objects.
                Each subtask object MUST have keys:
                - "title": concise action-oriented string
                - "description": clear technical description of the subtask
                - "priority": one of "LOW", "MEDIUM", "HIGH", "URGENT"
                - "estimatedStoryPoints": integer estimate (1, 2, 3, 5, or 8)
                """;

        String prompt = String.format(
                "Decompose this task into exactly %d subtasks:\nTitle: %s\nDescription: %s\nStatus: %s\nPriority: %s",
                effectiveCount,
                task.title(),
                task.description() != null ? task.description() : "None",
                task.status().name(),
                task.priority().name()
        );

        String jsonResult = geminiApiClient.generateContent(systemInstruction, prompt);
        List<SuggestedSubtask> subtasks = parseSubtasksFromJson(jsonResult);

        return new AiTaskBreakdownResponse(taskId, subtasks);
    }

    public SprintVelocityForecastResponse forecastSprintVelocity(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        checkProjectMemberOrAdmin(sprint.projectId());

        List<Sprint> allProjectSprints = sprintRepository.findByProjectId(sprint.projectId());
        List<Sprint> completedSprints = allProjectSprints.stream()
                .filter(s -> s.status() == SprintStatus.COMPLETED)
                .toList();

        List<Task> currentSprintTasks = taskRepository.findBySprintId(sprintId);
        double plannedCapacity = currentSprintTasks.size();

        if (completedSprints.isEmpty()) {
            return new SprintVelocityForecastResponse(
                    sprintId,
                    0.0,
                    plannedCapacity,
                    0.0,
                    "MEDIUM",
                    List.of("Insufficient sprint history for automated velocity calculation (Minimum 1 completed sprint required)")
            );
        }

        double totalCompletedTasks = 0;
        for (Sprint compSprint : completedSprints) {
            List<Task> compTasks = taskRepository.findBySprintId(compSprint.id());
            totalCompletedTasks += compTasks.stream().filter(t -> t.status() == TaskStatus.DONE).count();
        }

        double historicalAvgVelocity = Math.round((totalCompletedTasks / completedSprints.size()) * 10.0) / 10.0;
        double completionRate = plannedCapacity > 0
                ? Math.min(100.0, Math.round((historicalAvgVelocity / plannedCapacity) * 1000.0) / 10.0)
                : 100.0;

        String systemInstruction = """
                You are an Agile Velocity Analyst. Evaluate the sprint metrics and provide qualitative risk analysis.
                You MUST return a JSON object with keys:
                - "riskLevel": one of "LOW", "MEDIUM", "HIGH"
                - "aiInsights": an array of short, actionable insight strings (2-4 bullets)
                """;

        String prompt = String.format(
                "Sprint: %s\nCompleted Sprints History Count: %d\nHistorical Average Velocity (Tasks/Sprint): %.1f\nCurrent Sprint Planned Capacity (Tasks): %.1f\nForecasted Completion Rate: %.1f%%\nTotal Planned Tasks: %d",
                sprint.name(),
                completedSprints.size(),
                historicalAvgVelocity,
                plannedCapacity,
                completionRate,
                currentSprintTasks.size()
        );

        String riskLevel = completionRate < 70.0 ? "HIGH" : (completionRate < 90.0 ? "MEDIUM" : "LOW");
        List<String> insights = new ArrayList<>();

        try {
            String jsonResult = geminiApiClient.generateContent(systemInstruction, prompt);
            JsonNode root = objectMapper.readTree(jsonResult);
            if (root.has("riskLevel")) {
                riskLevel = root.get("riskLevel").asText().toUpperCase();
            }
            if (root.has("aiInsights") && root.get("aiInsights").isArray()) {
                for (JsonNode node : root.get("aiInsights")) {
                    insights.add(node.asText());
                }
            }
        } catch (Exception e) {
            log.warn("Gemini velocity analysis parsing failed, using deterministic insights", e);
            if (insights.isEmpty()) {
                insights.add(String.format("Planned capacity (%.1f tasks) vs historical velocity (%.1f tasks).", plannedCapacity, historicalAvgVelocity));
                if (completionRate < 80.0) {
                    insights.add("High risk of scope overflow based on historical completion trends.");
                } else {
                    insights.add("Sprint commitments align well with historical team velocity.");
                }
            }
        }

        return new SprintVelocityForecastResponse(
                sprintId,
                historicalAvgVelocity,
                plannedCapacity,
                completionRate,
                riskLevel,
                insights
        );
    }

    public StandupSummaryResponse generateStandupSummary(UUID projectId, Integer timeWindowHours) {
        checkProjectMemberOrAdmin(projectId);

        int effectiveHours = (timeWindowHours != null && timeWindowHours > 0) ? timeWindowHours : 24;
        Instant since = Instant.now().minus(effectiveHours, ChronoUnit.HOURS);

        List<TaskActivityLog> logs = activityLogRepository.findByProjectIdAndWindow(projectId, since);
        List<Task> projectTasks = taskRepository.findByProjectId(projectId);

        List<String> completedWork = new ArrayList<>();
        List<String> inProgressWork = new ArrayList<>();
        List<String> blockersAndRisks = new ArrayList<>();

        for (TaskActivityLog log : logs) {
            if ("STATUS_CHANGED".equals(log.eventType()) && "DONE".equals(log.newValue())) {
                completedWork.add("Task updated to DONE (ID: " + (log.taskId() != null ? log.taskId() : "N/A") + ")");
            }
        }

        for (Task task : projectTasks) {
            if (task.status() == TaskStatus.IN_PROGRESS || task.status() == TaskStatus.IN_REVIEW) {
                inProgressWork.add(task.title() + " (" + task.status().name() + ")");
            }
            if (task.priority() == TaskPriority.URGENT && task.status() != TaskStatus.DONE) {
                blockersAndRisks.add("URGENT: " + task.title() + " (" + task.status().name() + ")");
            }
        }

        if (completedWork.isEmpty()) {
            completedWork.add("No tasks completed in the last " + effectiveHours + " hours.");
        }
        if (inProgressWork.isEmpty()) {
            inProgressWork.add("No tasks currently in progress.");
        }
        if (blockersAndRisks.isEmpty()) {
            blockersAndRisks.add("No high priority blockers identified.");
        }

        String systemInstruction = """
                You are an Agile Project Manager. Synthesize daily standup activity into a clean Markdown summary report.
                Provide structured Markdown covering Completed Work, In Progress, and Blockers/Risks.
                You MUST return a JSON object with key "generatedSummary" containing the markdown text string.
                """;

        String prompt = String.format(
                "Project ID: %s\nTime Window: %d hours\nCompleted Items:\n- %s\n\nIn Progress Items:\n- %s\n\nBlockers/Risks:\n- %s",
                projectId,
                effectiveHours,
                String.join("\n- ", completedWork),
                String.join("\n- ", inProgressWork),
                String.join("\n- ", blockersAndRisks)
        );

        String generatedMarkdown = "### Daily Standup Summary (" + effectiveHours + "h)\n\n" +
                "**Completed:**\n- " + String.join("\n- ", completedWork) + "\n\n" +
                "**In Progress:**\n- " + String.join("\n- ", inProgressWork) + "\n\n" +
                "**Blockers / Risks:**\n- " + String.join("\n- ", blockersAndRisks);

        try {
            String jsonResult = geminiApiClient.generateContent(systemInstruction, prompt);
            JsonNode root = objectMapper.readTree(jsonResult);
            if (root.has("generatedSummary")) {
                generatedMarkdown = root.get("generatedSummary").asText();
            }
        } catch (Exception e) {
            log.warn("Gemini standup summary generation failed, using fallback template", e);
        }

        return new StandupSummaryResponse(
                projectId,
                effectiveHours,
                completedWork,
                inProgressWork,
                blockersAndRisks,
                generatedMarkdown
        );
    }

    private List<SuggestedSubtask> parseSubtasksFromJson(String jsonText) {
        List<SuggestedSubtask> subtasks = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonText);
            JsonNode arrayNode = root.has("suggestedSubtasks") ? root.get("suggestedSubtasks") : root;
            if (arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    String title = node.has("title") ? node.get("title").asText() : "Subtask";
                    String desc = node.has("description") ? node.get("description").asText() : "";
                    TaskPriority priority = TaskPriority.MEDIUM;
                    if (node.has("priority")) {
                        try {
                            priority = TaskPriority.valueOf(node.get("priority").asText().toUpperCase());
                        } catch (Exception ignored) {}
                    }
                    int points = node.has("estimatedStoryPoints") ? node.get("estimatedStoryPoints").asInt(2) : 2;
                    subtasks.add(new SuggestedSubtask(title, desc, priority, points));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse subtasks from JSON: {}", jsonText, e);
            subtasks.add(new SuggestedSubtask("Analyze subtask requirements", "Auto-generated subtask placeholder", TaskPriority.MEDIUM, 2));
        }
        return subtasks;
    }
}
