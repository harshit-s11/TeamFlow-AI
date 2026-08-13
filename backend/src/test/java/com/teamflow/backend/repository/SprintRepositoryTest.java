package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.SprintStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SprintRepositoryTest {

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void saveAndFindByProjectId_persistsSprintWithDatesAndStatus() {
        Project project = projectRepository.save(Project.create("Sprint Test Project", "Desc"));
        Sprint sprint = Sprint.create(
                project.id(),
                "Sprint 1",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                SprintStatus.ACTIVE
        );

        Sprint saved = sprintRepository.save(sprint);
        assertThat(saved.id()).isNotNull();
        assertThat(saved.status()).isEqualTo(SprintStatus.ACTIVE);

        List<Sprint> sprints = sprintRepository.findByProjectId(project.id());
        assertThat(sprints).hasSize(1);
        assertThat(sprints.get(0).name()).isEqualTo("Sprint 1");
    }
}
