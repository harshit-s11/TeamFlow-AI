package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Project;
import com.teamflow.backend.domain.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Project> projectRowMapper = (rs, rowNum) -> new Project(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toInstant()
    );

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toInstant()
    );

    public Project save(Project project) {
        if (project.id() == null) {
            String sql = "INSERT INTO projects (name, description, created_at) VALUES (?, ?, ?) RETURNING id, created_at";
            Timestamp createdAtTimestamp = project.createdAt() != null ? Timestamp.from(project.createdAt()) : new Timestamp(System.currentTimeMillis());

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Project(
                    rs.getObject("id", UUID.class),
                    project.name(),
                    project.description(),
                    rs.getTimestamp("created_at").toInstant()
            ), project.name(), project.description(), createdAtTimestamp);
        } else {
            String sql = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(sql, project.name(), project.description(), project.id());
            return project;
        }
    }

    public Optional<Project> findById(UUID id) {
        String sql = "SELECT id, name, description, created_at FROM projects WHERE id = ?";
        try {
            Project project = jdbcTemplate.queryForObject(sql, projectRowMapper, id);
            return Optional.ofNullable(project);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean addMember(UUID projectId, UUID userId) {
        String sql = "INSERT INTO project_members (project_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        return jdbcTemplate.update(sql, projectId, userId) > 0;
    }

    public boolean removeMember(UUID projectId, UUID userId) {
        String sql = "DELETE FROM project_members WHERE project_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, projectId, userId) > 0;
    }

    public List<User> findMembers(UUID projectId) {
        String sql = """
            SELECT u.id, u.name, u.email, u.created_at
            FROM users u
            JOIN project_members pm ON u.id = pm.user_id
            WHERE pm.project_id = ?
            ORDER BY pm.joined_at ASC
        """;
        return jdbcTemplate.query(sql, userRowMapper, projectId);
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
