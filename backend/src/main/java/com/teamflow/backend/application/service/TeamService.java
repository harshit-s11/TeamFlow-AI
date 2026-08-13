package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.Team;
import com.teamflow.backend.repository.TeamRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        UUID creatorId = SecurityUtils.getCurrentUserId();
        Team team = Team.create(request.name());
        Team savedTeam = teamRepository.save(team);

        teamRepository.addMember(savedTeam.id(), creatorId);
        return TeamResponse.fromDomain(savedTeam);
    }

    public TeamResponse getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        checkTeamMemberOrAdmin(id);
        return TeamResponse.fromDomain(team);
    }

    public List<TeamResponse> getAllTeams() {
        if (SecurityUtils.isAdmin()) {
            return teamRepository.findAll()
                    .stream()
                    .map(TeamResponse::fromDomain)
                    .toList();
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        return teamRepository.findByMemberId(userId)
                .stream()
                .map(TeamResponse::fromDomain)
                .toList();
    }

    public TeamResponse updateTeam(UUID id, TeamUpdateRequest request) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        checkTeamMemberOrAdmin(id);

        Team teamToSave = new Team(existingTeam.id(), request.name(), existingTeam.createdAt());
        Team updatedTeam = teamRepository.save(teamToSave);
        return TeamResponse.fromDomain(updatedTeam);
    }

    public void deleteTeam(UUID id) {
        teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        checkTeamMemberOrAdmin(id);

        teamRepository.deleteById(id);
    }

    public List<UserResponse> getTeamMembers(UUID teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        checkTeamMemberOrAdmin(teamId);

        return teamRepository.findMembers(teamId)
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    public UserResponse addMember(UUID teamId, UUID userId) {
        return addTeamMember(teamId, userId);
    }

    public UserResponse addTeamMember(UUID teamId, UUID userId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        checkTeamMemberOrAdmin(teamId);

        try {
            teamRepository.addMember(teamId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User is already a member of this team");
        }

        return userRepository.findById(userId)
                .map(UserResponse::fromDomain)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public void removeMember(UUID teamId, UUID userId) {
        removeTeamMember(teamId, userId);
    }

    public void removeTeamMember(UUID teamId, UUID userId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        checkTeamMemberOrAdmin(teamId);

        if (!teamRepository.isMember(teamId, userId)) {
            throw new ResourceNotFoundException("User is not a member of team with id: " + teamId);
        }

        teamRepository.removeMember(teamId, userId);
    }

    private void checkTeamMemberOrAdmin(UUID teamId) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!teamRepository.isMember(teamId, userId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
