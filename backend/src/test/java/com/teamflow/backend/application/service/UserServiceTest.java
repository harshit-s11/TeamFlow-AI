package com.teamflow.backend.application.service;

import com.teamflow.backend.api.dto.UserCreateRequest;
import com.teamflow.backend.api.dto.UserResponse;
import com.teamflow.backend.api.dto.UserUpdateRequest;
import com.teamflow.backend.common.exception.DuplicateResourceException;
import com.teamflow.backend.common.exception.ResourceNotFoundException;
import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private void setAuthUser(UUID userId, String role) {
        UserAccount account = new UserAccount(userId, "Test User", "test@teamflow.com", "hash", role, Instant.now());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                account, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_whenAdmin_returnsUserResponse() {
        setAuthUser(UUID.randomUUID(), "ADMIN");
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
    void createUser_whenRegularUser_throwsAccessDeniedException() {
        setAuthUser(UUID.randomUUID(), "USER");
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createUser_whenPreCheckDuplicateEmail_throwsDuplicateResourceException() {
        setAuthUser(UUID.randomUUID(), "ADMIN");
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");
        User existingUser = new User(UUID.randomUUID(), "Grace", "grace@teamflow.com", Instant.now());

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("grace@teamflow.com");
    }

    @Test
    void createUser_whenDatabaseDataIntegrityViolation_throwsDuplicateResourceException() {
        setAuthUser(UUID.randomUUID(), "ADMIN");
        UserCreateRequest request = new UserCreateRequest("Grace", "grace@teamflow.com");

        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("Unique index constraint"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getUserById_whenSelf_returnsUserResponse() {
        UUID userId = UUID.randomUUID();
        setAuthUser(userId, "USER");
        User user = new User(userId, "Self User", "self@teamflow.com", Instant.now());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertThat(response.id()).isEqualTo(userId);
    }

    @Test
    void getUserById_whenDifferentUser_throwsAccessDeniedException() {
        UUID authUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        setAuthUser(authUserId, "USER");

        assertThatThrownBy(() -> userService.getUserById(otherUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserById_whenUserNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getAllUsers_whenAdmin_returnsAllUsers() {
        setAuthUser(UUID.randomUUID(), "ADMIN");
        User user1 = new User(UUID.randomUUID(), "U1", "u1@teamflow.com", Instant.now());
        given(userRepository.findAll()).willReturn(List.of(user1));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
    }

    @Test
    void getAllUsers_whenRegularUser_throwsAccessDeniedException() {
        setAuthUser(UUID.randomUUID(), "USER");

        assertThatThrownBy(() -> userService.getAllUsers())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateUser_whenValidSelf_returnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
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
    void deleteUser_whenUserExistsSelf_deletesUser() {
        UUID id = UUID.randomUUID();
        setAuthUser(id, "USER");
        User existingUser = new User(id, "To Delete", "delete@teamflow.com", Instant.now());

        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }
}
