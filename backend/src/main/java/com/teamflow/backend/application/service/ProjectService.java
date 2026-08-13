package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.ProjectCreateRequest;
import com.teamflow.backend.api.dto.ProjectResponse;
import com.teamflow.backend.api.dto.ProjectUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        Project project = Project.create(request.name(), request.description());
        Project saved = projectRepository.save(project);
        return ProjectResponse.fromDomain(saved);
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return ProjectResponse.fromDomain(project);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::fromDomain)
                .toList();
    }

    public ProjectResponse updateProject(UUID id, ProjectUpdateRequest request) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Project updatedProject = new Project(existingProject.id(), request.name(), request.description(), existingProject.createdAt());
        Project saved = projectRepository.save(updatedProject);
        return ProjectResponse.fromDomain(saved);
    }

    public void deleteProject(UUID id) {
        if (projectRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    public void addMember(UUID projectId, UUID userId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        try {
            projectRepository.addMember(projectId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User with id " + userId + " is already a member of project " + projectId);
        }
    }

    public void removeMember(UUID projectId, UUID userId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        boolean removed = projectRepository.removeMember(projectId, userId);
        if (!removed) {
            throw new ResourceNotFoundException("User with id " + userId + " is not a member of project " + projectId);
        }
    }

    public List<UserResponse> getProjectMembers(UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return projectRepository.findMembers(projectId)
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }
}
