package com.teamflow.backend.api.controller;

import com.teamflow.backend.application.security.JwtService;
import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.Team;
import com.teamflow.backend.domain.model.UserAccount;
import com.teamflow.backend.repository.ProjectRepository;
import com.teamflow.backend.repository.SprintRepository;
import com.teamflow.backend.repository.TaskRepository;
import com.teamflow.backend.repository.TeamRepository;
import com.teamflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TeamRepository teamRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private SprintRepository sprintRepository;

    @MockitoBean
    private TaskRepository taskRepository;

    private MockMvc mockMvc;

    private UUID userAId;
    private UUID userBId;
    private UUID adminId;

    private UserAccount userAccountA;
    private UserAccount userAccountB;
    private UserAccount adminAccount;

    private String tokenA;
    private String tokenB;
    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userAId = UUID.randomUUID();
        userBId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        userAccountA = new UserAccount(userAId, "User A", "usera@teamflow.com", "hash", "USER", Instant.now());
        userAccountB = new UserAccount(userBId, "User B", "userb@teamflow.com", "hash", "USER", Instant.now());
        adminAccount = new UserAccount(adminId, "Admin", "admin@teamflow.com", "hash", "ADMIN", Instant.now());

        tokenA = jwtService.generateToken(userAccountA);
        tokenB = jwtService.generateToken(userAccountB);
        tokenAdmin = jwtService.generateToken(adminAccount);

        given(userRepository.findAccountByEmail("usera@teamflow.com")).willReturn(Optional.of(userAccountA));
        given(userRepository.findAccountByEmail("userb@teamflow.com")).willReturn(Optional.of(userAccountB));
        given(userRepository.findAccountByEmail("admin@teamflow.com")).willReturn(Optional.of(adminAccount));
    }

    @Test
    void authentication_expiredToken_returnsUnauthorized401() throws Exception {
        JwtService expiredJwtService = new JwtService("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", -1000);
        String expiredToken = expiredJwtService.generateToken(userAccountA);

        mockMvc.perform(get("/api/v1/users/" + userAId)
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authentication_malformedToken_returnsUnauthorized401() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userAId)
                        .header("Authorization", "Bearer malformed.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void users_regularUserGetOtherUser_returnsForbidden403() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void users_regularUserGetAllUsers_returnsForbidden403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void users_adminGetAllUsers_returnsOk200() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void idor_userB_accessingUserATeam_returnsForbidden403() throws Exception {
        UUID teamAId = UUID.randomUUID();
        given(teamRepository.findById(teamAId)).willReturn(Optional.of(new Team(teamAId, "Team A", Instant.now())));
        given(teamRepository.isMember(teamAId, userBId)).willReturn(false);

        mockMvc.perform(get("/api/v1/teams/" + teamAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void idor_userB_accessingUserAProject_returnsForbidden403() throws Exception {
        UUID projectAId = UUID.randomUUID();
        given(projectRepository.findById(projectAId)).willReturn(Optional.of(new Project(projectAId, "Project A", "Desc", Instant.now())));
        given(projectRepository.isMember(projectAId, userBId)).willReturn(false);

        mockMvc.perform(get("/api/v1/projects/" + projectAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void scopedRoute_userB_accessingProjectTasks_returnsForbidden403() throws Exception {
        UUID projectAId = UUID.randomUUID();
        given(projectRepository.findById(projectAId)).willReturn(Optional.of(new Project(projectAId, "Project A", "Desc", Instant.now())));
        given(projectRepository.isMember(projectAId, userBId)).willReturn(false);

        mockMvc.perform(get("/api/v1/projects/" + projectAId + "/tasks")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void scopedRoute_userB_accessingSprintTasks_returnsForbidden403() throws Exception {
        UUID sprintAId = UUID.randomUUID();
        UUID projectAId = UUID.randomUUID();
        given(sprintRepository.findById(sprintAId)).willReturn(Optional.of(new com.teamflow.backend.domain.model.Sprint(sprintAId, projectAId, "Sprint 1", java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(14), com.teamflow.backend.domain.model.SprintStatus.PLANNED, Instant.now())));
        given(projectRepository.isMember(projectAId, userBId)).willReturn(false);

        mockMvc.perform(get("/api/v1/sprints/" + sprintAId + "/tasks")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
