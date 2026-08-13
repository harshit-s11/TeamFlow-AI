package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindTask_withForeignKeys_persistsSuccessfully() {
        Project project = projectRepository.save(Project.create("Task Project", "Desc"));
        User user = userRepository.save(User.create("Frank", "frank@teamflow.com"));
        Sprint sprint = sprintRepository.save(Sprint.create(
                project.id(), "Sprint A", LocalDate.now(), LocalDate.now().plusDays(7), SprintStatus.PLANNED
        ));

        Task task = Task.create(
                project.id(),
                sprint.id(),
                user.id(),
                "Implement Task Domain Model",
                "Create task entity and database migration",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH
        );

        Task saved = taskRepository.save(task);
        assertThat(saved.id()).isNotNull();
        assertThat(saved.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(saved.priority()).isEqualTo(TaskPriority.HIGH);

        Optional<Task> found = taskRepository.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().title()).isEqualTo("Implement Task Domain Model");

        List<Task> projectTasks = taskRepository.findByProjectId(project.id());
        assertThat(projectTasks).hasSize(1);

        List<Task> sprintTasks = taskRepository.findBySprintId(sprint.id());
        assertThat(sprintTasks).hasSize(1);

        List<Task> userTasks = taskRepository.findByAssignedUserId(user.id());
        assertThat(userTasks).hasSize(1);
    }
}
