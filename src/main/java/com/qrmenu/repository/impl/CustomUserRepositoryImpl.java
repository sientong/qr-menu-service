package com.qrmenu.repository.impl;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.repository.CustomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;


import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .passwordHash(rs.getString("password_hash"))
            .role(UserRole.valueOf(rs.getString("role")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public User saveUser(User user) {
        if (user.getId() == null) {
            return insert(user);
        }
        return update(user);
    }

    private User insert(User user) {
        String sql = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated ID");
        }
        user.setId(key.longValue());
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, userRowMapper, email)
                .stream()
                .findFirst();
    }

    @Override
    public void delete(Long id) {
        String sql = "UPDATE users SET active = false WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public User update(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getPasswordHash(), user.getRole().name(), user.getId());
        return user;
    }
    // ... rest of the implementation from UserRepositoryImpl
} 