package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.TaskActivityLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public class TaskActivityLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TaskActivityLog> rowMapper = (rs, rowNum) -> new TaskActivityLog(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("project_id")),
            rs.getString("task_id") != null ? UUID.fromString(rs.getString("task_id")) : null,
            UUID.fromString(rs.getString("actor_user_id")),
            rs.getString("event_type"),
            rs.getString("field_changed"),
            rs.getString("old_value"),
            rs.getString("new_value"),
            rs.getTimestamp("created_at").toInstant()
    );

    public TaskActivityLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TaskActivityLog save(TaskActivityLog log) {
        String sql = """
            INSERT INTO task_activity_logs (project_id, task_id, actor_user_id, event_type, field_changed, old_value, new_value, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, project_id, task_id, actor_user_id, event_type, field_changed, old_value, new_value, created_at
            """;

        return jdbcTemplate.queryForObject(
                sql,
                rowMapper,
                log.projectId(),
                log.taskId(),
                log.actorUserId(),
                log.eventType(),
                log.fieldChanged(),
                log.oldValue(),
                log.newValue(),
                Timestamp.from(log.createdAt())
        );
    }

    public List<TaskActivityLog> findByTaskIdOrderByCreatedAtAsc(UUID taskId) {
        String sql = """
            SELECT id, project_id, task_id, actor_user_id, event_type, field_changed, old_value, new_value, created_at
            FROM task_activity_logs
            WHERE task_id = ?
            ORDER BY created_at ASC
            """;
        return jdbcTemplate.query(sql, rowMapper, taskId);
    }

    public List<TaskActivityLog> findByProjectIdAndWindow(UUID projectId, java.time.Instant since) {
        String sql = """
            SELECT id, project_id, task_id, actor_user_id, event_type, field_changed, old_value, new_value, created_at
            FROM task_activity_logs
            WHERE project_id = ? AND created_at >= ?
            ORDER BY created_at ASC
            """;
        return jdbcTemplate.query(sql, rowMapper, projectId, Timestamp.from(since));
    }
}
