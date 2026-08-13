package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.Team;
import com.teamflow.backend.repository.TeamRepository;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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

    public TeamResponse createTeam(TeamCreateRequest request) {
        Team team = Team.create(request.name());
        Team saved = teamRepository.save(team);
        return TeamResponse.fromDomain(saved);
    }

    public TeamResponse getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return TeamResponse.fromDomain(team);
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(TeamResponse::fromDomain)
                .toList();
    }

    public TeamResponse updateTeam(UUID id, TeamUpdateRequest request) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        Team updatedTeam = new Team(existingTeam.id(), request.name(), existingTeam.createdAt());
        Team saved = teamRepository.save(updatedTeam);
        return TeamResponse.fromDomain(saved);
    }

    public void deleteTeam(UUID id) {
        if (teamRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    public void addMember(UUID teamId, UUID userId) {
        if (teamRepository.findById(teamId).isEmpty()) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        try {
            teamRepository.addMember(teamId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User with id " + userId + " is already a member of team " + teamId);
        }
    }

    public void removeMember(UUID teamId, UUID userId) {
        if (teamRepository.findById(teamId).isEmpty()) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        boolean removed = teamRepository.removeMember(teamId, userId);
        if (!removed) {
            throw new ResourceNotFoundException("User with id " + userId + " is not a member of team " + teamId);
        }
    }

    public List<UserResponse> getTeamMembers(UUID teamId) {
        if (teamRepository.findById(teamId).isEmpty()) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return teamRepository.findMembers(teamId)
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }
}
