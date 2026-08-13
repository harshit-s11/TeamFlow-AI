package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Team;
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
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindMembers_managesTeamMemberships() {
        Team team = teamRepository.save(Team.create("Engineering Team"));
        User user1 = userRepository.save(User.create("Charlie", "charlie@teamflow.com"));
        User user2 = userRepository.save(User.create("Dana", "dana@teamflow.com"));

        teamRepository.addMember(team.id(), user1.id());
        teamRepository.addMember(team.id(), user2.id());

        List<User> members = teamRepository.findMembers(team.id());
        assertThat(members).hasSize(2);
        assertThat(members).extracting(User::email).containsExactlyInAnyOrder("charlie@teamflow.com", "dana@teamflow.com");

        teamRepository.removeMember(team.id(), user1.id());
        List<User> remainingMembers = teamRepository.findMembers(team.id());
        assertThat(remainingMembers).hasSize(1);
        assertThat(remainingMembers.get(0).email()).isEqualTo("dana@teamflow.com");
    }

    @Test
    void isMember_whenMembershipExists_returnsTrue() {
        Team team = teamRepository.save(Team.create("Dev Team"));
        User user = userRepository.save(User.create("Alice", "alice.team@teamflow.com"));
        teamRepository.addMember(team.id(), user.id());

        boolean isMember = teamRepository.isMember(team.id(), user.id());

        assertThat(isMember).isTrue();
    }

    @Test
    void isMember_whenMembershipDoesNotExist_returnsFalse() {
        Team team = teamRepository.save(Team.create("Dev Team 2"));
        User user = userRepository.save(User.create("Bob", "bob.team@teamflow.com"));

        boolean isMember = teamRepository.isMember(team.id(), user.id());

        assertThat(isMember).isFalse();
    }
}
