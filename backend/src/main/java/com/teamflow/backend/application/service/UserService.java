package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.UserCreateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.dto.UserUpdateRequest;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.common.security.SecurityUtils;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
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
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("User with email already exists: " + request.email());
        }

        try {
            User user = User.create(request.name(), request.email());
            User savedUser = userRepository.save(user);
            return UserResponse.fromDomain(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("User with email already exists: " + request.email());
        }
    }

    public UserResponse getUserById(UUID id) {
        SecurityUtils.checkSelfOrAdmin(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromDomain(user);
    }

    public List<UserResponse> getAllUsers() {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        SecurityUtils.checkSelfOrAdmin(id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existingUser.email().equalsIgnoreCase(request.email())
                && userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("User with email already exists: " + request.email());
        }

        User userToSave = new User(existingUser.id(), request.name(), request.email(), existingUser.createdAt());
        User updatedUser = userRepository.save(userToSave);
        return UserResponse.fromDomain(updatedUser);
    }

    public void deleteUser(UUID id) {
        SecurityUtils.checkSelfOrAdmin(id);
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.deleteById(id);
    }
}
