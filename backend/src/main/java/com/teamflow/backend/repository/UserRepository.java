package com.teamflow.backend.repository;

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
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toInstant()
    );

    public User save(User user) {
        if (user.id() == null) {
            String sql = "INSERT INTO users (name, email, created_at) VALUES (?, ?, ?) RETURNING id, created_at";
            Timestamp createdAtTimestamp = user.createdAt() != null ? Timestamp.from(user.createdAt()) : new Timestamp(System.currentTimeMillis());

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new User(
                    rs.getObject("id", UUID.class),
                    user.name(),
                    user.email(),
                    rs.getTimestamp("created_at").toInstant()
            ), user.name(), user.email(), createdAtTimestamp);
        } else {
            String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.name(), user.email(), user.id());
            return user;
        }
    }

    public Optional<User> findById(UUID id) {
        String sql = "SELECT id, name, email, created_at FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, created_at FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        String sql = "SELECT id, name, email, created_at FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
