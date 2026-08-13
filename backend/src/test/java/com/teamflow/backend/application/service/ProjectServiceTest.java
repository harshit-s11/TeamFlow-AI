package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.ProjectCreateRequest;
import com.teamflow.backend.api.dto.ProjectResponse;
import com.teamflow.backend.api.dto.ProjectUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, userRepository);
    }

    @Test
    void createProject_whenValid_returnsProjectResponse() {
        ProjectCreateRequest request = new ProjectCreateRequest("AI Platform", "Core AI service engine");
        UUID projectId = UUID.randomUUID();
        Project savedProject = new Project(projectId, "AI Platform", "Core AI service engine", Instant.now());

        given(projectRepository.save(any(Project.class))).willReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("AI Platform");
        assertThat(response.description()).isEqualTo("Core AI service engine");
    }

    @Test
    void getProjectById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(projectRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateProject_whenValid_returnsUpdatedResponse() {
        UUID projectId = UUID.randomUUID();
        ProjectUpdateRequest request = new ProjectUpdateRequest("AI Engine Updated", "New desc");
        Project existingProject = new Project(projectId, "AI Engine", "Old desc", Instant.now());
        Project updatedProject = new Project(projectId, "AI Engine Updated", "New desc", existingProject.createdAt());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(existingProject));
        given(projectRepository.save(any(Project.class))).willReturn(updatedProject);

        ProjectResponse response = projectService.updateProject(projectId, request);

        assertThat(response.name()).isEqualTo("AI Engine Updated");
        assertThat(response.description()).isEqualTo("New desc");
    }

    @Test
    void deleteProject_whenProjectExists_deletesProject() {
        UUID id = UUID.randomUUID();
        Project existingProject = new Project(id, "To Delete", "Desc", Instant.now());

        given(projectRepository.findById(id)).willReturn(Optional.of(existingProject));

        projectService.deleteProject(id);

        verify(projectRepository).deleteById(id);
    }

    @Test
    void addMember_whenValid_addsUserToProject() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Project X", "Desc", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Kate", "kate@teamflow.com", Instant.now())));

        projectService.addMember(projectId, userId);

        verify(projectRepository).addMember(projectId, userId);
    }

    @Test
    void addMember_whenDuplicateMember_throwsDuplicateResourceException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Project X", "Desc", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Kate", "kate@teamflow.com", Instant.now())));
        given(projectRepository.addMember(projectId, userId)).willThrow(new DataIntegrityViolationException("Duplicate primary key"));

        assertThatThrownBy(() -> projectService.addMember(projectId, userId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void removeMember_whenMembershipDoesNotExist_throwsResourceNotFoundException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Project X", "Desc", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Kate", "kate@teamflow.com", Instant.now())));
        given(projectRepository.removeMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> projectService.removeMember(projectId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not a member");
    }
}
