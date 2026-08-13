package com.teamflow.backend.application.security;

import com.teamflow.backend.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 900000; // 15 mins

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secretKey, expirationMs);
    }

    @Test
    void generateToken_andExtractClaims_worksCorrectly() {
        UUID userId = UUID.randomUUID();
        UserAccount account = new UserAccount(userId, "Alice", "alice@teamflow.com", "hash", "USER", Instant.now());

        String token = jwtService.generateToken(account);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@teamflow.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        assertThat(jwtService.isTokenValid(token, account)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForDifferentUser() {
        UUID userId = UUID.randomUUID();
        UserAccount account1 = new UserAccount(userId, "Alice", "alice@teamflow.com", "hash", "USER", Instant.now());
        UserAccount account2 = new UserAccount(UUID.randomUUID(), "Bob", "bob@teamflow.com", "hash", "USER", Instant.now());

        String token = jwtService.generateToken(account1);

        assertThat(jwtService.isTokenValid(token, account2)).isFalse();
    }

    @Test
    void isTokenExpired_returnsTrueForExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(secretKey, -1000); // Expiry in past
        UserAccount account = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hash", "USER", Instant.now());

        String token = shortLivedJwtService.generateToken(account);

        assertThat(jwtService.isTokenExpired(token)).isTrue();
        assertThat(jwtService.isTokenValid(token, account)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        UserAccount account = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hash", "USER", Instant.now());

        assertThat(jwtService.isTokenValid("invalid.jwt.token", account)).isFalse();
    }
}
