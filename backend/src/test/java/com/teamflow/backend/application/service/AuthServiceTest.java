package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.AuthResponse;
import com.teamflow.backend.api.dto.LoginRequest;
import com.teamflow.backend.api.dto.RegisterRequest;
import com.teamflow.backend.application.security.JwtService;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_whenValid_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@teamflow.com", "Secret123!");
        UserAccount savedAccount = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hashedPassword", "USER", Instant.now());

        given(userRepository.findAccountByEmail(request.email())).willReturn(Optional.empty());
        given(passwordEncoder.encode(request.password())).willReturn("hashedPassword");
        given(userRepository.saveAccount(eq("Alice"), eq("alice@teamflow.com"), eq("hashedPassword"), eq("USER"))).willReturn(savedAccount);
        given(jwtService.generateToken(savedAccount)).willReturn("jwtToken123");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwtToken123");
        assertThat(response.user().email()).isEqualTo("alice@teamflow.com");
    }

    @Test
    void register_whenDuplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@teamflow.com", "Secret123!");
        UserAccount existingAccount = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hash", "USER", Instant.now());

        given(userRepository.findAccountByEmail(request.email())).willReturn(Optional.of(existingAccount));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_whenValidCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("alice@teamflow.com", "Secret123!");
        UserAccount account = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hashedPassword", "USER", Instant.now());

        given(userRepository.findAccountByEmail(request.email())).willReturn(Optional.of(account));
        given(passwordEncoder.matches("Secret123!", "hashedPassword")).willReturn(true);
        given(jwtService.generateToken(account)).willReturn("jwtToken123");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwtToken123");
        assertThat(response.user().email()).isEqualTo("alice@teamflow.com");
    }

    @Test
    void login_whenUnknownEmail_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("unknown@teamflow.com", "Secret123!");

        given(userRepository.findAccountByEmail(request.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_whenIncorrectPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("alice@teamflow.com", "WrongPassword!");
        UserAccount account = new UserAccount(UUID.randomUUID(), "Alice", "alice@teamflow.com", "hashedPassword", "USER", Instant.now());

        given(userRepository.findAccountByEmail(request.email())).willReturn(Optional.of(account));
        given(passwordEncoder.matches("WrongPassword!", "hashedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
