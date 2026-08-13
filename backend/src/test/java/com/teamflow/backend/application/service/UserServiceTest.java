package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.UserCreateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.dto.UserUpdateRequest;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void createUser_whenValid_returnsUserResponse() {
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");
        UUID userId = UUID.randomUUID();
        User savedUser = new User(userId, "Grace", "grace@teamflow.com", Instant.now());

        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Grace");
        assertThat(response.email()).isEqualTo("grace@teamflow.com");
    }

    @Test
    void createUser_whenPreCheckDuplicateEmail_throwsDuplicateResourceException() {
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");
        User existingUser = new User(UUID.randomUUID(), "Grace", "grace@teamflow.com", Instant.now());

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("grace@teamflow.com");
    }

    @Test
    void createUser_whenDatabaseDataIntegrityViolation_throwsDuplicateResourceException() {
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");

        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("Unique index constraint"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getUserById_whenUserNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateUser_whenValid_returnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("Updated Name", "updated@teamflow.com");
        User existingUser = new User(id, "Old Name", "old@teamflow.com", Instant.now());
        User updatedUser = new User(id, "Updated Name", "updated@teamflow.com", existingUser.createdAt());

        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        UserResponse response = userService.updateUser(id, request);

        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.email()).isEqualTo("updated@teamflow.com");
    }

    @Test
    void deleteUser_whenUserExists_deletesUser() {
        UUID id = UUID.randomUUID();
        User existingUser = new User(id, "To Delete", "delete@teamflow.com", Instant.now());

        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }
}
