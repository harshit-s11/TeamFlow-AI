package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.Team;
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
public class TeamRepository {

    private final JdbcTemplate jdbcTemplate;

    public TeamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Team> teamRowMapper = (rs, rowNum) -> new Team(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getTimestamp("created_at").toInstant()
    );

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toInstant()
    );

    public Team save(Team team) {
        if (team.id() == null) {
            String sql = "INSERT INTO teams (name, created_at) VALUES (?, ?) RETURNING id, created_at";
            Timestamp createdAtTimestamp = team.createdAt() != null ? Timestamp.from(team.createdAt()) : new Timestamp(System.currentTimeMillis());

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Team(
                    rs.getObject("id", UUID.class),
                    team.name(),
                    rs.getTimestamp("created_at").toInstant()
            ), team.name(), createdAtTimestamp);
        } else {
            String sql = "UPDATE teams SET name = ? WHERE id = ?";
            jdbcTemplate.update(sql, team.name(), team.id());
            return team;
        }
    }

    public Optional<Team> findById(UUID id) {
        String sql = "SELECT id, name, created_at FROM teams WHERE id = ?";
        try {
            Team team = jdbcTemplate.queryForObject(sql, teamRowMapper, id);
            return Optional.ofNullable(team);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean addMember(UUID teamId, UUID userId) {
        String sql = "INSERT INTO team_members (team_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        return jdbcTemplate.update(sql, teamId, userId) > 0;
    }

    public boolean removeMember(UUID teamId, UUID userId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, teamId, userId) > 0;
    }

    public List<User> findMembers(UUID teamId) {
        String sql = """
            SELECT u.id, u.name, u.email, u.created_at
            FROM users u
            JOIN team_members tm ON u.id = tm.user_id
            WHERE tm.team_id = ?
            ORDER BY tm.joined_at ASC
        """;
        return jdbcTemplate.query(sql, userRowMapper, teamId);
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM teams WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
