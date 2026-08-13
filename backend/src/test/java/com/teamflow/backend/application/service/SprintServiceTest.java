package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.SprintCreateRequest;
import com.teamflow.backend.api.dto.SprintResponse;
import com.teamflow.backend.api.dto.SprintUpdateRequest;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.SprintStatus;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectRepository projectRepository;

    private SprintService sprintService;

    private void setAuthUser(UUID userId, String role) {
        UserAccount account = new UserAccount(userId, "Test User", "test@teamflow.com", "hash", role, Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        sprintService = new SprintService(sprintRepository, projectRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSprint_whenProjectMember_returnsSprintResponse() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        SprintCreateRequest request = new SprintCreateRequest(
                projectId,
                "Sprint 1",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                SprintStatus.PLANNED
        );
        UUID sprintId = UUID.randomUUID();
        Sprint savedSprint = new Sprint(sprintId, projectId, "Sprint 1", request.startDate(), request.endDate(), SprintStatus.PLANNED, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(sprintRepository.save(any(Sprint.class))).willReturn(savedSprint);

        SprintResponse response = sprintService.createSprint(request);

        assertThat(response.id()).isEqualTo(sprintId);
        assertThat(response.name()).isEqualTo("Sprint 1");
        assertThat(response.status()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void createSprint_whenNonProjectMember_throwsAccessDeniedException() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        SprintCreateRequest request = new SprintCreateRequest(
                projectId,
                "Sprint 1",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                SprintStatus.PLANNED
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> sprintService.createSprint(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createSprint_whenEndDateBeforeStartDate_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        SprintCreateRequest request = new SprintCreateRequest(
                projectId,
                "Sprint Invalid Dates",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                SprintStatus.PLANNED
        );

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);

        assertThatThrownBy(() -> sprintService.createSprint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be before startDate");
    }

    @Test
    void getSprintById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        given(sprintRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.getSprintById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateSprint_whenProjectMember_returnsUpdatedResponse() {
        UUID userId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        SprintUpdateRequest request = new SprintUpdateRequest(
                "Sprint 1 Updated",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                SprintStatus.ACTIVE
        );
        Sprint existingSprint = new Sprint(sprintId, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now());
        Sprint updatedSprint = new Sprint(sprintId, projectId, "Sprint 1 Updated", request.startDate(), request.endDate(), SprintStatus.ACTIVE, existingSprint.createdAt());

        given(sprintRepository.findById(sprintId)).willReturn(Optional.of(existingSprint));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(sprintRepository.save(any(Sprint.class))).willReturn(updatedSprint);

        SprintResponse response = sprintService.updateSprint(sprintId, request);

        assertThat(response.name()).isEqualTo("Sprint 1 Updated");
        assertThat(response.status()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    void deleteSprint_whenProjectMember_deletesSprint() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Sprint existingSprint = new Sprint(id, projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now());

        given(sprintRepository.findById(id)).willReturn(Optional.of(existingSprint));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);

        sprintService.deleteSprint(id);

        verify(sprintRepository).deleteById(id);
    }

    @Test
    void getSprintsByProjectId_whenProjectMember_returnsSprintList() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Sprint sprint = new Sprint(UUID.randomUUID(), projectId, "Sprint 1", LocalDate.now(), LocalDate.now().plusDays(14), SprintStatus.PLANNED, Instant.now());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(new Project(projectId, "Proj", "Desc", Instant.now())));
        given(projectRepository.isMember(projectId, userId)).willReturn(true);
        given(sprintRepository.findByProjectId(projectId)).willReturn(List.of(sprint));

        List<SprintResponse> responses = sprintService.getSprintsByProjectId(projectId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Sprint 1");
    }

    @Test
    void getAllSprints_whenRegularUser_throwsAccessDeniedException() {
        setAuthUser(UUID.randomUUID(), "USER");

        assertThatThrownBy(() -> sprintService.getAllSprints())
                .isInstanceOf(AccessDeniedException.class);
    }
}
