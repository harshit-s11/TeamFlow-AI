package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.ProjectCreateRequest;
import com.teamflow.backend.api.dto.ProjectResponse;
import com.teamflow.backend.api.dto.ProjectUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        UUID creatorId = SecurityUtils.getCurrentUserId();
        Project project = Project.create(request.name(), request.description());
        Project savedProject = projectRepository.save(project);

        projectRepository.addMember(savedProject.id(), creatorId);
        return ProjectResponse.fromDomain(savedProject);
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        checkProjectMemberOrAdmin(id);
        return ProjectResponse.fromDomain(project);
    }

    public List<ProjectResponse> getAllProjects() {
        if (SecurityUtils.isAdmin()) {
            return projectRepository.findAll()
                    .stream()
                    .map(ProjectResponse::fromDomain)
                    .toList();
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        return projectRepository.findByMemberId(userId)
                .stream()
                .map(ProjectResponse::fromDomain)
                .toList();
    }

    public ProjectResponse updateProject(UUID id, ProjectUpdateRequest request) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        checkProjectMemberOrAdmin(id);

        Project projectToSave = new Project(existingProject.id(), request.name(), request.description(), existingProject.createdAt());
        Project updatedProject = projectRepository.save(projectToSave);
        return ProjectResponse.fromDomain(updatedProject);
    }

    public void deleteProject(UUID id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        checkProjectMemberOrAdmin(id);

        projectRepository.deleteById(id);
    }

    public List<UserResponse> getProjectMembers(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        checkProjectMemberOrAdmin(projectId);

        return projectRepository.findMembers(projectId)
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    public UserResponse addMember(UUID projectId, UUID userId) {
        return addProjectMember(projectId, userId);
    }

    public UserResponse addProjectMember(UUID projectId, UUID userId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        checkProjectMemberOrAdmin(projectId);

        try {
            projectRepository.addMember(projectId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        return userRepository.findById(userId)
                .map(UserResponse::fromDomain)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public void removeMember(UUID projectId, UUID userId) {
        removeProjectMember(projectId, userId);
    }

    public void removeProjectMember(UUID projectId, UUID userId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        checkProjectMemberOrAdmin(projectId);

        if (!projectRepository.isMember(projectId, userId)) {
            throw new ResourceNotFoundException("User is not a member of project with id: " + projectId);
        }

        projectRepository.removeMember(projectId, userId);
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
