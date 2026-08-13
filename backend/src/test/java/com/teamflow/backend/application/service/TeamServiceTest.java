package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Team;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.TeamRepository;
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
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, userRepository);
    }

    @Test
    void createTeam_whenValid_returnsTeamResponse() {
        TeamCreateRequest request = new TeamCreateRequest("Core Infra");
        UUID teamId = UUID.randomUUID();
        Team savedTeam = new Team(teamId, "Core Infra", Instant.now());

        given(teamRepository.save(any(Team.class))).willReturn(savedTeam);

        TeamResponse response = teamService.createTeam(request);

        assertThat(response.id()).isEqualTo(teamId);
        assertThat(response.name()).isEqualTo("Core Infra");
    }

    @Test
    void getTeamById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(teamRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateTeam_whenValid_returnsUpdatedResponse() {
        UUID teamId = UUID.randomUUID();
        TeamUpdateRequest request = new TeamUpdateRequest("Platform Team");
        Team existingTeam = new Team(teamId, "Old Infra", Instant.now());
        Team updatedTeam = new Team(teamId, "Platform Team", existingTeam.createdAt());

        given(teamRepository.findById(teamId)).willReturn(Optional.of(existingTeam));
        given(teamRepository.save(any(Team.class))).willReturn(updatedTeam);

        TeamResponse response = teamService.updateTeam(teamId, request);

        assertThat(response.name()).isEqualTo("Platform Team");
    }

    @Test
    void deleteTeam_whenTeamExists_deletesTeam() {
        UUID id = UUID.randomUUID();
        Team existingTeam = new Team(id, "To Delete", Instant.now());

        given(teamRepository.findById(id)).willReturn(Optional.of(existingTeam));

        teamService.deleteTeam(id);

        verify(teamRepository).deleteById(id);
    }

    @Test
    void addMember_whenValid_addsUserToTeam() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team(teamId, "Devs", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Ivy", "ivy@teamflow.com", Instant.now())));

        teamService.addMember(teamId, userId);

        verify(teamRepository).addMember(teamId, userId);
    }

    @Test
    void addMember_whenDuplicateMember_throwsDuplicateResourceException() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team(teamId, "Devs", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Ivy", "ivy@teamflow.com", Instant.now())));
        given(teamRepository.addMember(teamId, userId)).willThrow(new DataIntegrityViolationException("Duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> teamService.addMember(teamId, userId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void removeMember_whenMembershipDoesNotExist_throwsResourceNotFoundException() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team(teamId, "Devs", Instant.now())));
        given(userRepository.findById(userId)).willReturn(Optional.of(new User(userId, "Ivy", "ivy@teamflow.com", Instant.now())));
        given(teamRepository.removeMember(teamId, userId)).willReturn(false);

        assertThatThrownBy(() -> teamService.removeMember(teamId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not a member");
    }
}
