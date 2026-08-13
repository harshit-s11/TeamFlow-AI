package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.AuthResponse;
import com.teamflow.backend.api.dto.LoginRequest;
import com.teamflow.backend.api.dto.RegisterRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.application.security.JwtService;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findAccountByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        UserAccount account = userRepository.saveAccount(request.name(), request.email(), passwordHash, "USER");

        String token = jwtService.generateToken(account);
        UserResponse userResponse = UserResponse.fromDomain(new User(account.id(), account.name(), account.email(), account.createdAt()));

        return new AuthResponse(token, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount account = userRepository.findAccountByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (account.passwordHash() == null || !passwordEncoder.matches(request.password(), account.passwordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(account);
        UserResponse userResponse = UserResponse.fromDomain(new User(account.id(), account.name(), account.email(), account.createdAt()));

        return new AuthResponse(token, userResponse);
    }
}
