package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.SprintCreateRequest;
import com.teamflow.backend.api.dto.SprintResponse;
import com.teamflow.backend.api.dto.SprintUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import org.springframework.stereotype.Service;

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
        if (projectRepository.findById(request.projectId()).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + request.projectId());
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }

        Sprint sprint = Sprint.create(
                request.projectId(),
                request.name(),
                request.startDate(),
                request.endDate(),
                request.status()
        );

        Sprint saved = sprintRepository.save(sprint);
        return SprintResponse.fromDomain(saved);
    }

    public SprintResponse getSprintById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));
        return SprintResponse.fromDomain(sprint);
    }

    public List<SprintResponse> getAllSprints() {
        return sprintRepository.findAll()
                .stream()
                .map(SprintResponse::fromDomain)
                .toList();
    }

    public List<SprintResponse> getSprintsByProjectId(UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return sprintRepository.findByProjectId(projectId)
                .stream()
                .map(SprintResponse::fromDomain)
                .toList();
    }

    public SprintResponse updateSprint(UUID id, SprintUpdateRequest request) {
        Sprint existingSprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }

        Sprint updatedSprint = new Sprint(
                existingSprint.id(),
                existingSprint.projectId(),
                request.name(),
                request.startDate(),
                request.endDate(),
                request.status(),
                existingSprint.createdAt()
        );

        Sprint saved = sprintRepository.save(updatedSprint);
        return SprintResponse.fromDomain(saved);
    }

    public void deleteSprint(UUID id) {
        if (sprintRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Sprint not found with id: " + id);
        }
        sprintRepository.deleteById(id);
    }
}
