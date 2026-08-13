package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Sprint;
import com.teamflow.backend.domain.model.SprintStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SprintRepository {

    private final JdbcTemplate jdbcTemplate;

    public SprintRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Sprint> sprintRowMapper = (rs, rowNum) -> new Sprint(
            rs.getObject("id", UUID.class),
            rs.getObject("project_id", UUID.class),
            rs.getString("name"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            SprintStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toInstant()
    );

    public Sprint save(Sprint sprint) {
        if (sprint.id() == null) {
            String sql = "INSERT INTO sprints (project_id, name, start_date, end_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
            Timestamp createdAtTimestamp = sprint.createdAt() != null ? Timestamp.from(sprint.createdAt()) : new Timestamp(System.currentTimeMillis());

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Sprint(
                    rs.getObject("id", UUID.class),
                    sprint.projectId(),
                    sprint.name(),
                    sprint.startDate(),
                    sprint.endDate(),
                    sprint.status(),
                    rs.getTimestamp("created_at").toInstant()
            ), sprint.projectId(), sprint.name(), Date.valueOf(sprint.startDate()), Date.valueOf(sprint.endDate()), sprint.status().name(), createdAtTimestamp);
        } else {
            String sql = "UPDATE sprints SET name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(sql, sprint.name(), Date.valueOf(sprint.startDate()), Date.valueOf(sprint.endDate()), sprint.status().name(), sprint.id());
            return sprint;
        }
    }

    public Optional<Sprint> findById(UUID id) {
        String sql = "SELECT id, project_id, name, start_date, end_date, status, created_at FROM sprints WHERE id = ?";
        try {
            Sprint sprint = jdbcTemplate.queryForObject(sql, sprintRowMapper, id);
            return Optional.ofNullable(sprint);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Sprint> findByProjectId(UUID projectId) {
        String sql = "SELECT id, project_id, name, start_date, end_date, status, created_at FROM sprints WHERE project_id = ? ORDER BY start_date ASC";
        return jdbcTemplate.query(sql, sprintRowMapper, projectId);
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM sprints WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
