package com.teamflow.backend.api.controller;

import com.teamflow.backend.api.dto.TeamCreateRequest;
import com.teamflow.backend.api.dto.TeamResponse;
import com.teamflow.backend.api.dto.TeamUpdateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.application.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        TeamResponse created = teamService.createTeam(request);
        URI location = URI.create("/api/v1/teams/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody TeamUpdateRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> addMember(@PathVariable UUID teamId, @PathVariable UUID userId) {
        teamService.addMember(teamId, userId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID teamId, @PathVariable UUID userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<UserResponse>> getTeamMembers(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamMembers(teamId));
    }
}
