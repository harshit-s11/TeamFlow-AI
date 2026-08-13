package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.SprintCreateRequest;
import com.teamflow.backend.api.dto.SprintResponse;
import com.teamflow.backend.api.dto.SprintUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.SprintStatus;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    public SprintService(SprintRepository sprintRepository, ProjectRepository projectRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    public SprintResponse createSprint(SprintCreateRequest request) {
        projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));

        checkProjectMemberOrAdmin(request.projectId());
        validateDates(request.startDate(), request.endDate());

        SprintStatus status = request.status() != null ? request.status() : SprintStatus.PLANNED;
        Sprint sprint = Sprint.create(
                request.projectId(),
                request.name(),
                request.startDate(),
                request.endDate(),
                status
        );

        Sprint savedSprint = sprintRepository.save(sprint);
        return SprintResponse.fromDomain(savedSprint);
    }

    public SprintResponse getSprintById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        checkProjectMemberOrAdmin(sprint.projectId());
        return SprintResponse.fromDomain(sprint);
    }

    public List<SprintResponse> getSprintsByProjectId(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        checkProjectMemberOrAdmin(projectId);

        return sprintRepository.findByProjectId(projectId)
                .stream()
                .map(SprintResponse::fromDomain)
                .toList();
    }

    public List<SprintResponse> getAllSprints() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        return sprintRepository.findAll()
                .stream()
                .map(SprintResponse::fromDomain)
                .toList();
    }

    public SprintResponse updateSprint(UUID id, SprintUpdateRequest request) {
        Sprint existingSprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        checkProjectMemberOrAdmin(existingSprint.projectId());
        validateDates(request.startDate(), request.endDate());

        SprintStatus status = request.status() != null ? request.status() : existingSprint.status();
        Sprint sprintToSave = new Sprint(
                existingSprint.id(),
                existingSprint.projectId(),
                request.name(),
                request.startDate(),
                request.endDate(),
                status,
                existingSprint.createdAt()
        );

        Sprint updatedSprint = sprintRepository.save(sprintToSave);
        return SprintResponse.fromDomain(updatedSprint);
    }

    public void deleteSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        checkProjectMemberOrAdmin(sprint.projectId());
        sprintRepository.deleteById(id);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Sprint endDate cannot be before startDate");
        }
    }

    private void checkProjectMemberOrAdmin(UUID projectId) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!projectRepository.isMember(projectId, userId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
