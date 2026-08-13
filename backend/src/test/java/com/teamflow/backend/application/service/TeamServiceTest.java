package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Team;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.TeamRepository;
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
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    private TeamService teamService;

    private void setAuthUser(UUID userId, String role) {
        UserAccount account = new UserAccount(userId, "Test User", "test@teamflow.com", "hash", role, Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTeam_whenValid_returnsTeamResponseAndAddsCreatorAsMember() {
        UUID creatorId = UUID.randomUUID();
        setAuthUser(creatorId, "USER");
        TeamCreateRequest request = new TeamCreateRequest("Core Infra");
        UUID teamId = UUID.randomUUID();
        Team savedTeam = new Team(teamId, "Core Infra", Instant.now());

        given(teamRepository.save(any(Team.class))).willReturn(savedTeam);

        TeamResponse response = teamService.createTeam(request);

        assertThat(response.id()).isEqualTo(teamId);
        assertThat(response.name()).isEqualTo("Core Infra");
        verify(teamRepository).addMember(teamId, creatorId);
    }

    @Test
    void getTeamById_whenMember_returnsTeam() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Team team = new Team(teamId, "Devs", Instant.now());

        given(teamRepository.findById(teamId)).willReturn(Optional.of(team));
        given(teamRepository.isMember(teamId, userId)).willReturn(true);

        TeamResponse response = teamService.getTeamById(teamId);

        assertThat(response.id()).isEqualTo(teamId);
    }

    @Test
    void getTeamById_whenNonMember_throwsAccessDeniedException() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Team team = new Team(teamId, "Devs", Instant.now());

        given(teamRepository.findById(teamId)).willReturn(Optional.of(team));
        given(teamRepository.isMember(teamId, userId)).willReturn(false);

        assertThatThrownBy(() -> teamService.getTeamById(teamId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTeamById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        given(teamRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateTeam_whenMember_returnsUpdatedResponse() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        TeamUpdateRequest request = new TeamUpdateRequest("Platform Team");
        Team existingTeam = new Team(teamId, "Old Infra", Instant.now());
        Team updatedTeam = new Team(teamId, "Platform Team", existingTeam.createdAt());

        given(teamRepository.findById(teamId)).willReturn(Optional.of(existingTeam));
        given(teamRepository.isMember(teamId, userId)).willReturn(true);
        given(teamRepository.save(any(Team.class))).willReturn(updatedTeam);

        TeamResponse response = teamService.updateTeam(teamId, request);

        assertThat(response.name()).isEqualTo("Platform Team");
    }

    @Test
    void deleteTeam_whenMember_deletesTeam() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        Team existingTeam = new Team(teamId, "To Delete", Instant.now());

        given(teamRepository.findById(teamId)).willReturn(Optional.of(existingTeam));
        given(teamRepository.isMember(teamId, userId)).willReturn(true);

        teamService.deleteTeam(teamId);

        verify(teamRepository).deleteById(teamId);
    }

    @Test
    void addTeamMember_whenValidMember_addsUserToTeam() {
        UUID authUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(authUserId, "USER");

        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team(teamId, "Devs", Instant.now())));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(new User(targetUserId, "Ivy", "ivy@teamflow.com", Instant.now())));
        given(teamRepository.isMember(teamId, authUserId)).willReturn(true);

        teamService.addTeamMember(teamId, targetUserId);

        verify(teamRepository).addMember(teamId, targetUserId);
    }

    @Test
    void addTeamMember_whenDuplicateMember_throwsDuplicateResourceException() {
        UUID authUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        setAuthUser(authUserId, "USER");

        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team(teamId, "Devs", Instant.now())));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(new User(targetUserId, "Ivy", "ivy@teamflow.com", Instant.now())));
        given(teamRepository.isMember(teamId, authUserId)).willReturn(true);
        given(teamRepository.addMember(teamId, targetUserId)).willThrow(new DataIntegrityViolationException("Duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> teamService.addTeamMember(teamId, targetUserId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already a member");
    }
}
