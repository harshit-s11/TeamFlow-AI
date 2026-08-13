package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Task;
import com.teamflow.backend.domain.model.TaskPriority;
import com.teamflow.backend.domain.model.TaskStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> new Task(
            rs.getObject("id", UUID.class),
            rs.getObject("project_id", UUID.class),
            rs.getObject("sprint_id", UUID.class),
            rs.getObject("assigned_user_id", UUID.class),
            rs.getString("title"),
            rs.getString("description"),
            TaskStatus.valueOf(rs.getString("status")),
            TaskPriority.valueOf(rs.getString("priority")),
            rs.getTimestamp("created_at").toInstant()
    );

    public Task save(Task task) {
        if (task.id() == null) {
            String sql = "INSERT INTO tasks (project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
            Timestamp createdAtTimestamp = task.createdAt() != null ? Timestamp.from(task.createdAt()) : new Timestamp(System.currentTimeMillis());

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Task(
                    rs.getObject("id", UUID.class),
                    task.projectId(),
                    task.sprintId(),
                    task.assignedUserId(),
                    task.title(),
                    task.description(),
                    task.status(),
                    task.priority(),
                    rs.getTimestamp("created_at").toInstant()
            ), task.projectId(), task.sprintId(), task.assignedUserId(), task.title(), task.description(), task.status().name(), task.priority().name(), createdAtTimestamp);
        } else {
            String sql = "UPDATE tasks SET sprint_id = ?, assigned_user_id = ?, title = ?, description = ?, status = ?, priority = ? WHERE id = ?";
            jdbcTemplate.update(sql, task.sprintId(), task.assignedUserId(), task.title(), task.description(), task.status().name(), task.priority().name(), task.id());
            return task;
        }
    }

    public Optional<Task> findById(UUID id) {
        String sql = "SELECT id, project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at FROM tasks WHERE id = ?";
        try {
            Task task = jdbcTemplate.queryForObject(sql, taskRowMapper, id);
            return Optional.ofNullable(task);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Task> findAll() {
        String sql = "SELECT id, project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at FROM tasks ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, taskRowMapper);
    }

    public List<Task> findByProjectId(UUID projectId) {
        String sql = "SELECT id, project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at FROM tasks WHERE project_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, taskRowMapper, projectId);
    }

    public List<Task> findBySprintId(UUID sprintId) {
        String sql = "SELECT id, project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at FROM tasks WHERE sprint_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, taskRowMapper, sprintId);
    }

    public List<Task> findByAssignedUserId(UUID userId) {
        String sql = "SELECT id, project_id, sprint_id, assigned_user_id, title, description, status, priority, created_at FROM tasks WHERE assigned_user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, taskRowMapper, userId);
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
