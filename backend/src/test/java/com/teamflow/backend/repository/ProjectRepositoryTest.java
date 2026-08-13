package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndManageMembers_worksCorrectly() {
        Project project = projectRepository.save(Project.create("Core Platform", "Main product engine"));
        User user = userRepository.save(User.create("Eve", "eve@teamflow.com"));

        projectRepository.addMember(project.id(), user.id());

        List<User> members = projectRepository.findMembers(project.id());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).email()).isEqualTo("eve@teamflow.com");
    }

    @Test
    void isMember_whenMembershipExists_returnsTrue() {
        Project project = projectRepository.save(Project.create("Platform Alpha", "Alpha project"));
        User user = userRepository.save(User.create("Frank", "frank.project@teamflow.com"));
        projectRepository.addMember(project.id(), user.id());

        boolean isMember = projectRepository.isMember(project.id(), user.id());

        assertThat(isMember).isTrue();
    }

    @Test
    void isMember_whenMembershipDoesNotExist_returnsFalse() {
        Project project = projectRepository.save(Project.create("Platform Beta", "Beta project"));
        User user = userRepository.save(User.create("Grace", "grace.project@teamflow.com"));

        boolean isMember = projectRepository.isMember(project.id(), user.id());

        assertThat(isMember).isFalse();
    }
}
