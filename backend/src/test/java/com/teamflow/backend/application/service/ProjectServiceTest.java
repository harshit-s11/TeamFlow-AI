package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.ProjectCreateRequest;
import com.teamflow.backend.api.dto.ProjectResponse;
import com.teamflow.backend.api.dto.ProjectUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private void setAuthUser(UUID userId, String role) {
        UserAccount account = new UserAccount(userId, "Test User", "test@teamflow.com", "hash", role, Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_whenValid_returnsProjectResponseAndAddsCreatorAsMember() {
        UUID creatorId = UUID.randomUUID();
        setAuthUser(creatorId, "USER");
        ProjectCreateRequest request = new ProjectCreateRequest("AI Platform", "Core AI service engine");
        UUID projectId = UUID.randomUUID();
        Project savedProject = new Project(projectId, "AI Platform", "Core AI service engine", Instant.now());

        given(projectRepository.save(any(Project.class))).willReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("AI Platform");
        assertThat(response.description()).isEqualTo("Core AI service engine");
        verify(projectRepository).addMember(projectId, creatorId);
    }

    @Test
    void getProjectById_whenMember_returnsProject() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Project project = new Project(projectId, "AI Engine", "Desc", Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);

        ProjectResponse response = projectService.getProjectById(projectId);

        assertThat(response.id()).isEqualTo(projectId);
    }

    @Test
    void getProjectById_whenNonMember_throwsAccessDeniedException() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Project project = new Project(projectId, "AI Engine", "Desc", Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(projectRepository.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getProjectById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        given(projectRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateProject_whenMember_returnsUpdatedResponse() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        ProjectUpdateRequest request = new ProjectUpdateRequest("AI Engine Updated", "New desc");
        Project existingProject = new Project(projectId, "AI Engine", "Old desc", Instant.now());
        Project updatedProject = new Project(projectId, "AI Engine Updated", "New desc", existingProject.createdAt());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(existingProject));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(projectRepository.save(any(Project.class))).willReturn(updatedProject);

        ProjectResponse response = projectService.updateProject(projectId, request);

        assertThat(response.name()).isEqualTo("AI Engine Updated");
        assertThat(response.description()).isEqualTo("New desc");
    }

    @Test
    void deleteProject_whenMember_deletesProject() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Project existingProject = new Project(projectId, "To Delete", "Desc", Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(existingProject));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);

        projectService.deleteProject(projectId);

        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void addProjectMember_whenValidMember_addsUserToProject() {
        UUID authUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(authUserId, "USER");

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Project X", "Desc", Instant.now())));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(new User(targetUserId, "Kate", "kate@teamflow.com", Instant.now())));
        given(projectRepository.isMember(projectId, authUserId)).willReturn(true);

        projectService.addProjectMember(projectId, targetUserId);

        verify(projectRepository).addMember(projectId, targetUserId);
    }

    @Test
    void addProjectMember_whenDuplicateMember_throwsDuplicateResourceException() {
        UUID authUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(authUserId, "USER");

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Project X", "Desc", Instant.now())));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(new User(targetUserId, "Kate", "kate@teamflow.com", Instant.now())));
        given(projectRepository.isMember(projectId, authUserId)).willReturn(true);
        given(projectRepository.addMember(projectId, targetUserId)).willThrow(new DataIntegrityViolationException("Duplicate primary key"));

        assertThatThrownBy(() -> projectService.addProjectMember(projectId, targetUserId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already a member");
    }
}
