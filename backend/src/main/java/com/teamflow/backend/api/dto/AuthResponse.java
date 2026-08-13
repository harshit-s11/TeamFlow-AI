package com.teamflow.backend.api.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}
