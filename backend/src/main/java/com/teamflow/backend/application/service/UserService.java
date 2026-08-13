package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.UserCreateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.dto.UserUpdateRequest;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        try {
            User user = User.create(request.name(), request.email());
            User saved = userRepository.save(user);
            return UserResponse.fromDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromDomain(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.findByEmail(request.email()).ifPresent(otherUser -> {
            if (!otherUser.id().equals(id)) {
                throw new DuplicateResourceException("User already exists with email: " + request.email());
            }
        });

        try {
            User updatedUser = new User(existingUser.id(), request.name(), request.email(), existingUser.createdAt());
            User saved = userRepository.save(updatedUser);
            return UserResponse.fromDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }
    }

    public void deleteUser(UUID id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
